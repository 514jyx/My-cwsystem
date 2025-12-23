package com.example.demo.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.demo.common.Result;
import com.example.demo.system.entity.Entries;
import com.example.demo.system.entity.Transactions;
import com.example.demo.system.service.IEntriesService;
import com.example.demo.system.service.ITransactionsService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <p>
 * 财务交易前端控制器（提供交易新增、分页查询、状态变更、分录查询等接口）
 * </p>
 *
 * @author jyxmn
 * @since 2025-12-09
 */
@RestController
@RequestMapping("/system/transactions")
@CrossOrigin // 解决跨域问题（前端和后端端口不同时需要）
public class TransactionsController {

    @Resource
    private IEntriesService entriesService;

    @Resource
    private ITransactionsService transactionsService;

    // 日期格式化器（String → LocalDate）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 1. 生成唯一交易编号（兜底版：不依赖 Service，直接 Controller 生成）
     */
    @GetMapping("/generate-no")
    public Result generateTransNo() {
        try {
            // 生成编号（时间戳方式，绝对唯一）
            String transNo = "TR" + System.currentTimeMillis();
            System.out.println("生成交易编号：" + transNo);

            // 🌟 关键修复：(Object) 强制转换，让 Java 调用 success(Object data) 方法
            return Result.success((Object) transNo);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("生成编号失败：" + e.getMessage());
        }
    }

    /**
     * 2. 新增交易（含关联分录）- 适配主表删除amount字段
     */
    @PostMapping("/add")
    public Result addTransactionWithEntries(@RequestBody TransactionWithEntriesDTO dto) {
        try {
            // 1. 校验核心参数（避免空指针）
            if (dto.getTransNo() == null || dto.getTransNo().trim().isEmpty()) {
                return Result.fail("交易编号不能为空");
            }
            if (dto.getTransDate() == null || dto.getTransDate().trim().isEmpty()) {
                return Result.fail("交易日期不能为空");
            }
            if (dto.getTransType() == null || dto.getTransType().trim().isEmpty()) {
                return Result.fail("交易类型不能为空");
            }
            if (dto.getEntriesList() == null || dto.getEntriesList().size() < 2) {
                return Result.fail("分录不能为空，且至少包含一借一贷");
            }
            // 校验分录列表（避免空分录）
            dto.setEntriesList(dto.getEntriesList().stream()
                    .filter(entryDTO -> entryDTO != null)
                    .collect(Collectors.toList()));
            if (dto.getEntriesList().size() < 2) {
                return Result.fail("分录列表不能包含空数据，且至少需一借一贷");
            }

            // 2. 封装交易数据（类型转换适配）
            Transactions transactions = new Transactions();
            transactions.setTransNo(dto.getTransNo());
            // 日期转换：String → LocalDate（兼容前端传递格式，单独捕获日期格式异常）
            try {
                transactions.setTransDate(LocalDate.parse(dto.getTransDate(), DATE_FORMATTER));
            } catch (DateTimeParseException e) {
                return Result.fail("交易日期格式错误，正确格式：yyyy-MM-dd（如：2025-12-19）");
            }
            transactions.setTransType(dto.getTransType());
            transactions.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
            // 默认状态为「草稿」（前端未传时赋值）
            transactions.setStatus(dto.getStatus() != null ? dto.getStatus() : "草稿");
            // 关联订单ID（如果有，前端传递则赋值，无则留空）
            transactions.setOrderId(dto.getOrderId());
            transactions.setWriteOffTargetId(dto.getWriteOffTargetId());

            // 3. 分录转换：EntriesDTO（前端参数）→ Entries（实体类）
            List<Entries> entriesList = convertEntriesDTOToEntity(dto.getEntriesList());

            // 4. 调用Service新增（原子操作：交易+分录，带事务+借贷校验）
            boolean success = transactionsService.addTransactionWithEntries(transactions, entriesList);
            // 封装Map返回（消息+交易编号），适配Result类
            if (success) {
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("msg", "交易新增成功");
                resultMap.put("transNo", dto.getTransNo()); // 返回交易编号，前端可展示
                resultMap.put("status", "success");
                System.out.println("凭证新增成功，交易编号：" + dto.getTransNo());
                return Result.success(resultMap);
            } else {
                System.out.println("凭证新增失败，交易编号：" + dto.getTransNo());
                return Result.fail("交易新增失败");
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail("业务异常：" + e.getMessage()); // 业务异常（借贷不平衡、参数无效等）
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("参数格式错误：" + e.getMessage()); // 其他格式错误
        }
    }

    /**
     * 3. 分页搜索交易
     */
    @GetMapping("/page")
    public Result queryTransactionPage(
            @RequestParam(required = false, defaultValue = "1") Integer pageNum,
            @RequestParam(required = false, defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String transNo,
            @RequestParam(required = false) String transType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate

    ){
        try {
            IPage<Transactions> transactionPage = transactionsService.queryTransactionPage(
                    pageNum, pageSize, transNo, transType, status, startDate, endDate);
            // 封装分页结果（数据+总数），适配Result类
            Map<String, Object> pageResult = new HashMap<>();
            pageResult.put("list", transactionPage.getRecords()); // 分页数据列表
            pageResult.put("total", transactionPage.getTotal()); // 总条数
            pageResult.put("pageNum", pageNum); // 当前页码
            pageResult.put("pageSize", pageSize); // 每页条数
            return Result.success(pageResult);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("分页查询失败：" + e.getMessage());
        }
    }

    /**
     * 5. 查询交易关联分录
     */
    @GetMapping("/entries/{transactionId}")
    public Result getEntriesByTransactionId(@PathVariable Long transactionId) {
        try {
            List<Map<String, Object>> entries = transactionsService.getEntriesByTransactionId(transactionId);
            return entries.isEmpty() ? Result.fail("该交易无关联分录") : Result.success(entries);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("分录查询失败：" + e.getMessage());
        }
    }

    /**
     * 6. 删除交易（级联删除分录）
     */
    @DeleteMapping("/delete/{id}")
    public Result removeTransactionWithEntries(@PathVariable Long id) {
        try {
            boolean success = transactionsService.removeTransactionWithEntries(id);
            return success ? Result.success("交易删除成功") : Result.fail("交易删除失败或交易不存在");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("交易删除失败：" + e.getMessage());
        }
    }

    /**
     * 7. 查询最近已过账交易
     */
    @GetMapping("/recent")
    public Result getRecentTransactions(@RequestParam(required = false, defaultValue = "10") Integer limit) {
        try {
            List<Transactions> recentTransactions = transactionsService.getRecentTransactions(limit);
            return Result.success(recentTransactions);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("最近交易查询失败：" + e.getMessage());
        }
    }

    /**
     * 8. 根据ID查询交易
     */
    @GetMapping("/{id}")
    public Result getTransactionById(@PathVariable Long id) {
        try {
            // 1. 查询交易主表
            Transactions transaction = transactionsService.getById(id);
            if (transaction == null) {
                return Result.fail("交易不存在");
            }
            // 2. 查询关联的分录列表（调用已有的getEntriesByTransactionId方法）
            List<Map<String, Object>> entriesList = transactionsService.getEntriesByTransactionId(id);
            // 3. 封装交易+分录，返回给前端
            Map<String, Object> result = new HashMap<>();
            result.put("id", transaction.getId());
            result.put("transNo", transaction.getTransNo());
            result.put("transDate", transaction.getTransDate().format(DATE_FORMATTER)); // 转为String格式（yyyy-MM-dd）
            result.put("transType", transaction.getTransType());
            result.put("description", transaction.getDescription());
            result.put("status", transaction.getStatus());
            result.put("isWriteOff", transaction.getWriteOffTargetId() != null); // 标记是否冲销凭证
            result.put("writeOffTargetId", transaction.getWriteOffTargetId());
            result.put("entriesList", entriesList); // 关键：返回分录列表
            return Result.success(result);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("交易查询失败：" + e.getMessage());
        }
    }

    /**
     * 编辑凭证（仅限草稿状态）
     */
    @PutMapping("/update/{id}")
    public Result updateTransactionWithEntries(
            @PathVariable Long id,
            @RequestBody TransactionWithEntriesDTO dto) {
        try {
            // 1. 校验交易是否存在，且状态为草稿
            Transactions existingTrans = transactionsService.getById(id);
            if (existingTrans == null) {
                return Result.fail("凭证不存在");
            }
            if (!"草稿".equals(existingTrans.getStatus())) {
                return Result.fail("仅草稿状态的凭证可编辑");
            }

            // 2. 校验前端参数（和新增接口一致）
            if (dto.getTransDate() == null || dto.getTransDate().trim().isEmpty()) {
                return Result.fail("交易日期不能为空");
            }
            if (dto.getTransType() == null || dto.getTransType().trim().isEmpty()) {
                return Result.fail("交易类型不能为空");
            }
            if (dto.getEntriesList() == null || dto.getEntriesList().size() < 2) {
                return Result.fail("分录不能为空，且至少包含一借一贷");
            }

            // 3. 封装更新后的交易数据
            existingTrans.setTransDate(LocalDate.parse(dto.getTransDate(), DATE_FORMATTER));
            existingTrans.setTransType(dto.getTransType());
            existingTrans.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
            existingTrans.setOrderId(dto.getOrderId());
            existingTrans.setWriteOffTargetId(dto.getWriteOffTargetId());

            // 4. 转换分录数据
            List<Entries> entriesList = convertEntriesDTOToEntity(dto.getEntriesList());

            // 5. 调用Service更新（先删除原分录，再新增新分录）
            boolean success = transactionsService.updateTransactionWithEntries(existingTrans, entriesList);
            if (success) {
                return Result.success("凭证更新成功");
            } else {
                return Result.fail("凭证更新失败");
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            return Result.fail("交易日期格式错误，正确格式：yyyy-MM-dd");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("更新失败：" + e.getMessage());
        }
    }

    /**
     * 变更交易状态（简化版：仅变更状态，不更新余额）
     */
    @PutMapping("/update-status/{id}/{status}")
    @Transactional // 事务注解：确保状态更新原子操作
    public Result updateTransactionStatus(
            @PathVariable Long id,
            @PathVariable String status) {
        try {
            // 校验状态合法性（仅允许草稿→已过账）
            if (!"已过账".equals(status)) {
                return Result.fail("状态只能是「已过账」（人工审核后变更）");
            }

            // 校验凭证是否存在
            Transactions voucher = transactionsService.getById(id);
            if (voucher == null) {
                return Result.fail("交易不存在");
            }

            // 仅允许从草稿状态过账
            if (!"草稿".equals(voucher.getStatus())) {
                return Result.fail("仅待审核（草稿）状态的凭证可审核过账");
            }

            // 直接更新状态（删除余额更新相关代码）
            boolean success = transactionsService.updateTransactionStatus(id, status);
            return success ? Result.success("审核过账成功") : Result.fail("审核过账失败");
        } catch (RuntimeException e) {
            e.printStackTrace();
            return Result.fail(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail("审核过账失败：" + e.getMessage());
        }
    }

    // ======================== DTO类（接收前端参数，与表结构适配）========================
    /**
     * 交易+分录组合DTO：专门接收前端新增交易请求
     */
    static class TransactionWithEntriesDTO {
        private String transNo;         // 交易编号（前端从/generate-no接口获取）
        private String transDate;       // 交易日期（前端传递："2025-12-15"）
        private String transType;       // 交易类型（前端传递："采购"/"销售"/"收款"等）
        private String description;     // 交易说明（可选）
        private String status;          // 状态（可选，默认"草稿"）
        private Long orderId;
        private Long writeOffTargetId;
        private List<EntriesDTO> entriesList; // 分录列表（必传，至少2条）

        // Getter 和 Setter（完整，SpringMVC自动绑定参数）
        public Long getWriteOffTargetId() { return writeOffTargetId; }
        public void setWriteOffTargetId(Long writeOffTargetId) { this.writeOffTargetId = writeOffTargetId; }
        public String getTransNo() { return transNo; }
        public void setTransNo(String transNo) { this.transNo = transNo; }
        public String getTransDate() { return transDate; }
        public void setTransDate(String transDate) { this.transDate = transDate; }
        public String getTransType() { return transType; }
        public void setTransType(String transType) { this.transType = transType; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public List<EntriesDTO> getEntriesList() { return entriesList; }
        public void setEntriesList(List<EntriesDTO> entriesList) { this.entriesList = entriesList; }
    }

    /**
     * 分录子DTO：接收前端传递的分录参数
     */
    static class EntriesDTO {
        private Long accountId;    // 关联科目ID（必传）
        private String entryType;  // 借贷方向（必传）
        private Double amount;     // 分录金额（必传：大于0的数字）
        private String description; // 分录说明（可选）

        // Getter 和 Setter（完整）
        public Long getAccountId() { return accountId; }
        public void setAccountId(Long accountId) { this.accountId = accountId; }
        public String getEntryType() { return entryType; }
        public void setEntryType(String entryType) { this.entryType = entryType; }
        public Double getAmount() { return amount; }
        public void setAmount(Double amount) { this.amount = amount; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
    }

    /**
     * 将 EntriesDTO 列表转为 Entries 实体列表
     */
    private List<Entries> convertEntriesDTOToEntity(List<EntriesDTO> dtoList) {
        List<Entries> entriesList = new ArrayList<>();
        for (EntriesDTO dto : dtoList) {
            // 校验分录核心参数
            if (dto.getAccountId() == null) {
                throw new RuntimeException("某条分录的科目ID不能为空");
            }
            if (dto.getEntryType() == null || (!"借方".equals(dto.getEntryType()) && !"贷方".equals(dto.getEntryType()))) {
                throw new RuntimeException("某条分录的类型只能是「借方」或「贷方」");
            }
            if (dto.getAmount() == null || dto.getAmount() <= 0) {
                throw new RuntimeException("某条分录的金额必须大于0（当前值：" + dto.getAmount() + "）");
            }

            Entries entries = new Entries();
            entries.setAccountId(dto.getAccountId()); // 仅存科目ID（关联数据库）
            entries.setEntryType(dto.getEntryType());
            entries.setAmount(BigDecimal.valueOf(dto.getAmount()));
            entries.setDescription(dto.getDescription() != null ? dto.getDescription() : "");
            entriesList.add(entries);
        }
        return entriesList;
    }
}