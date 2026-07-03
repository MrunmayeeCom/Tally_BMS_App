import 'package:flutter/material.dart';
import 'package:bmstally_app/models/item.dart';
import 'package:bmstally_app/models/aggregated_item_data.dart';
import 'package:bmstally_app/services/mock_data_service.dart';

enum ItemViewMode { list, grid }

enum ItemFilter {
  all('Show All'),
  inStock('In Stock'),
  notInStock('Not In Stock'),
  negativeStock('Negative Stock'),
  belowReorder('Below Reorder Level');

  final String label;
  const ItemFilter(this.label);
}

class ItemsProvider extends ChangeNotifier {
  final MockDataService _dataService = MockDataService();

  ItemViewMode _viewMode = ItemViewMode.list;
  int _categoryTab = 0;
  ItemFilter _filter = ItemFilter.all;
  String _searchQuery = '';
  List<Item> _allItems = [];
  String _tenantId = '';
  static const _tabs = ['Item', 'Group', 'Category'];

  void setTenantId(String id) {
    _tenantId = id;
  }

  ItemViewMode get viewMode => _viewMode;
  int get categoryTab => _categoryTab;
  ItemFilter get filter => _filter;
  String get searchQuery => _searchQuery;
  List<String> get tabs => _tabs;

  List<Item> get items {
    var result = List<Item>.from(_allItems);
    if (_searchQuery.isNotEmpty) {
      final q = _searchQuery.toLowerCase();
      result = result.where((i) =>
        i.name.toLowerCase().contains(q) ||
        i.category.toLowerCase().contains(q) ||
        i.group.toLowerCase().contains(q)
      ).toList();
    }
    switch (_filter) {
      case ItemFilter.inStock:
        result = result.where((i) => i.isInStock).toList();
      case ItemFilter.notInStock:
        result = result.where((i) => !i.isInStock).toList();
      case ItemFilter.negativeStock:
        result = result.where((i) => i.quantity < 0).toList();
      case ItemFilter.belowReorder:
        result = result.where((i) => i.isBelowReorderLevel).toList();
      case ItemFilter.all:
        break;
    }
    return result;
  }

  List<AggregatedItemData> get groupData {
    final filtered = items;
    final map = <String, List<Item>>{};
    for (final item in filtered) {
      final g = item.group.isEmpty ? 'Uncategorized' : item.group;
      map.putIfAbsent(g, () => []).add(item);
    }
    return map.entries.map((e) => AggregatedItemData(
      name: e.key,
      itemCount: e.value.length,
      totalQuantity: e.value.fold(0, (sum, i) => sum + i.quantity),
    )).toList()
      ..sort((a, b) => a.name.compareTo(b.name));
  }

  List<AggregatedItemData> get categoryData {
    final filtered = items;
    final map = <String, List<Item>>{};
    for (final item in filtered) {
      map.putIfAbsent(item.category, () => []).add(item);
    }
    return map.entries.map((e) => AggregatedItemData(
      name: e.key,
      itemCount: e.value.length,
      totalQuantity: e.value.fold(0, (sum, i) => sum + i.quantity),
    )).toList()
      ..sort((a, b) => a.name.compareTo(b.name));
  }

  int get totalQuantity =>
      _allItems.fold(0, (sum, i) => sum + i.quantity.toInt());

  String get summaryLabel {
    switch (_categoryTab) {
      case 1: return 'Total Groups';
      case 2: return 'Total Categories';
      default: return 'Total Items';
    }
  }

  int get summaryCount {
    switch (_categoryTab) {
      case 1: return groupData.length;
      case 2: return categoryData.length;
      default: return items.length;
    }
  }

  void loadItems() {
    if (_tenantId.isEmpty) return;
    _allItems = _dataService.getItems(_tenantId);
    notifyListeners();
  }

  void toggleView() {
    _viewMode = _viewMode == ItemViewMode.list ? ItemViewMode.grid : ItemViewMode.list;
    notifyListeners();
  }

  void setCategoryTab(int index) {
    _categoryTab = index;
    _viewMode = ItemViewMode.list;
    notifyListeners();
  }

  void setFilter(ItemFilter filter) {
    _filter = filter;
    notifyListeners();
  }

  void setSearchQuery(String query) {
    _searchQuery = query;
    notifyListeners();
  }

  void addItem(Item item) {
    _allItems.insert(0, item);
    notifyListeners();
  }
}
