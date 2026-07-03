class SalesEntry {
  final String title;
  final String description;
  final String actionLabel;
  final bool hasArrow;

  const SalesEntry({
    required this.title,
    required this.description,
    this.actionLabel = '',
    this.hasArrow = false,
  });
}
