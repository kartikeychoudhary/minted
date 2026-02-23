package com.minted.api.statement.dto;

import java.math.BigDecimal;

public class ParsedTransactionRow {

    private String tempId;
    private BigDecimal amount;
    private String type;
    private String description;
    private String transactionDate;
    private String categoryName;
    private Long matchedCategoryId;
    private String notes;
    private String tags;
    private boolean isDuplicate;
    private String duplicateReason;
    private boolean mappedByRule;

    public ParsedTransactionRow() {}

    public ParsedTransactionRow(String tempId, BigDecimal amount, String type, String description,
                                String transactionDate, String categoryName, Long matchedCategoryId,
                                String notes, String tags, boolean isDuplicate, String duplicateReason,
                                boolean mappedByRule) {
        this.tempId = tempId;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
        this.categoryName = categoryName;
        this.matchedCategoryId = matchedCategoryId;
        this.notes = notes;
        this.tags = tags;
        this.isDuplicate = isDuplicate;
        this.duplicateReason = duplicateReason;
        this.mappedByRule = mappedByRule;
    }

    public String getTempId() { return tempId; }
    public void setTempId(String tempId) { this.tempId = tempId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getTransactionDate() { return transactionDate; }
    public void setTransactionDate(String transactionDate) { this.transactionDate = transactionDate; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public Long getMatchedCategoryId() { return matchedCategoryId; }
    public void setMatchedCategoryId(Long matchedCategoryId) { this.matchedCategoryId = matchedCategoryId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public boolean getIsDuplicate() { return isDuplicate; }
    public void setIsDuplicate(boolean isDuplicate) { this.isDuplicate = isDuplicate; }

    public String getDuplicateReason() { return duplicateReason; }
    public void setDuplicateReason(String duplicateReason) { this.duplicateReason = duplicateReason; }

    public boolean getMappedByRule() { return mappedByRule; }
    public void setMappedByRule(boolean mappedByRule) { this.mappedByRule = mappedByRule; }
}
