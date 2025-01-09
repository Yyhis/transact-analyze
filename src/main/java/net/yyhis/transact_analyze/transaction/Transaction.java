package net.yyhis.transact_analyze.transaction;

public class Transaction {
    public String transactionDate; // 거래일시
    public String description; // 적요
    public String recipient; // 보낸분/ 받는분
    public int withdrawalAmount; // 출금액
    public int depositAmount; // 입금액
    public String remittanceMemo; // 송금메모
}