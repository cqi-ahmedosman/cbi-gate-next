package com.cannyquest.participants.issuing;

public enum SvfeTrxType {

    PURCHASE (774, "Purchase"),
    CASHWITHDRAW(700, "Cash Withdrawal"),
    PREAUTH (736, "Pre-authorisation"),
    REFUND (775, "Refund"),
    BALANCEINQUIRY(702, "Balance Inquiry"),
    MINISTATEMENT (704, "Mini-statement"),
    COMPLETION(737, "Completion"),
    CASHADVANCE(700,"Cash Advance"),
    SignOn(801,"Cash Advance"),
    SignOff(802,"Cash Advance"),
    Echo (803,"Cash Advance"),
    UNKNOWN(000,"Unknown"),
    BILLPAYMENT(508,"bill payment"),
    BILLINQUIRY(511,"bill inquiry")
    ;

    private int val;
    private String description;

    SvfeTrxType(int val, String description) {
        this.val = val;
        this.description = description;
    }
    public int intValue() {
        return val;
    }
    public String toString () {
        return description;
    }
}
