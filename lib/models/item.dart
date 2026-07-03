class Item {
  final String id;
  final String name;
  final String category;
  final String group;
  final String unit;
  final double quantity;
  final double openingStock;
  final double purchaseRate;
  final double salesRate;
  final String description;
  final double reorderLevel;

  const Item({
    required this.id,
    required this.name,
    required this.category,
    this.group = '',
    this.unit = 'Nos',
    this.quantity = 0,
    this.openingStock = 0,
    this.purchaseRate = 0,
    this.salesRate = 0,
    this.description = '',
    this.reorderLevel = 0,
  });

  bool get isInStock => quantity > 0;
  bool get isBelowReorderLevel => reorderLevel > 0 && quantity < reorderLevel;
}
