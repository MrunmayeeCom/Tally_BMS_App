class OutstandingItem {
  final String partyName;
  final String creditInfo;
  final String amount;
  final bool isCredit;
  final String meta;
  final String paymentInfo;

  const OutstandingItem({
    required this.partyName,
    required this.creditInfo,
    required this.amount,
    required this.isCredit,
    this.meta = '',
    this.paymentInfo = '',
  });
}
