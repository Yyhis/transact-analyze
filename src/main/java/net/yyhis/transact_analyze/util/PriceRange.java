package net.yyhis.transact_analyze.util;

import java.util.List;
import java.util.stream.Collectors;

import net.yyhis.transact_analyze.transaction.Transaction;

public class PriceRange {
    public String priceRangeConvert(double priceRange) {
        if (Math.abs(priceRange - 3.5) < 0.0001) {
            return "30~50";
        } else if (Math.abs(priceRange - 5.1) < 0.0001) {

            return "50~100";
        } else if (Math.abs(priceRange - 10.0) < 0.0001) {

            return "100~";
        }
        return "";
    }

    public List<Transaction> priceRangeFillter(List<Transaction> result, double priceRange) {
        List<Transaction> filteredTransactions = result;
        // priceRange == 3.5 # Double형이라 부동소수점 고려
        // 가격 범위에 따른 필터링
        if (Math.abs(priceRange - 3.5) < 0.0001) {
            filteredTransactions = filterTransactions(result, 300000, 500000);
        } else if (Math.abs(priceRange - 5.1) < 0.0001) {
            filteredTransactions = filterTransactions(result, 500000, 1000000);
        } else if (Math.abs(priceRange - 10.0) < 0.0001) {
            filteredTransactions = result.stream()
                    .filter(t -> t.withdrawalAmount >= 1000000 || t.depositAmount >= 1000000)
                    .collect(Collectors.toList());
        }

        return filteredTransactions;
    }

    private List<Transaction> filterTransactions(List<Transaction> result, double min, double max) {
        return result.stream()
                .filter(t -> (t.withdrawalAmount >= min && t.withdrawalAmount < max) ||
                        (t.depositAmount >= min && t.depositAmount < max))
                .collect(Collectors.toList());
    }
}
