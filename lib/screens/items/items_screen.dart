import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/items_provider.dart';
import 'package:bmstally_app/models/aggregated_item_data.dart';
import 'package:bmstally_app/widgets/item_list_view.dart';
import 'package:bmstally_app/widgets/item_grid_view.dart';
import 'package:bmstally_app/widgets/item_filter_popup.dart';
import 'package:bmstally_app/widgets/share_bottom_sheet.dart';
import 'package:bmstally_app/screens/items/create_item_screen.dart';

class ItemsScreen extends StatefulWidget {
  const ItemsScreen({super.key});

  @override
  State<ItemsScreen> createState() => _ItemsScreenState();
}

class _ItemsScreenState extends State<ItemsScreen> {
  bool _showSearch = false;
  final _searchController = TextEditingController();

  @override
  void initState() {
    super.initState();
    context.read<ItemsProvider>().loadItems();
  }

  @override
  void dispose() {
    _searchController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ItemsProvider>();

    return Scaffold(
      body: Column(
        children: [
          _appBar(context, provider),
          _searchBar(provider),
          _summaryHeader(provider),
          _tabBar(provider),
          Expanded(child: _body(provider)),
        ],
      ),
      floatingActionButton: provider.categoryTab == 0
          ? FloatingActionButton.extended(
              backgroundColor: Colors.orange,
              foregroundColor: Colors.white,
              onPressed: () {
                Navigator.of(context).push(
                  MaterialPageRoute(builder: (_) => const CreateItemScreen()),
                );
              },
              icon: const Icon(Icons.add),
              label: const Text('Create Item'),
            )
          : null,
    );
  }

  Widget _body(ItemsProvider provider) {
    switch (provider.categoryTab) {
      case 1:
        return _aggregatedList(provider.groupData, Icons.group_work, 'Groups');
      case 2:
        return _aggregatedList(provider.categoryData, Icons.category, 'Categories');
      default:
        return provider.viewMode == ItemViewMode.list
            ? const ItemListView()
            : const ItemGridView();
    }
  }

  Widget _aggregatedList(
      List<AggregatedItemData> data, IconData icon, String label) {
    if (data.isEmpty) {
      return const Center(child: Text('No data found'));
    }

    return Column(
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          color: Colors.grey[100],
          child: Row(
            children: [
              Text(label,
                  style: TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 12,
                      color: Colors.grey[800])),
              const Spacer(),
              Text('Items',
                  style: TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 12,
                      color: Colors.grey[800])),
              const SizedBox(width: 24),
              Text('Qty',
                  style: TextStyle(
                      fontWeight: FontWeight.w600,
                      fontSize: 12,
                      color: Colors.grey[800])),
            ],
          ),
        ),
        Expanded(
          child: ListView.builder(
            itemCount: data.length,
            itemBuilder: (_, i) {
              final d = data[i];
              return Card(
                margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
                elevation: 0,
                color: Colors.grey[50],
                shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(10)),
                child: Padding(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                  child: Row(
                    children: [
                      CircleAvatar(
                        backgroundColor: Colors.indigo.withValues(alpha: 0.1),
                        child: Icon(icon, color: Colors.indigo, size: 20),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(d.name,
                            style:
                                const TextStyle(fontWeight: FontWeight.w600)),
                      ),
                      SizedBox(
                        width: 60,
                        child: Text('${d.itemCount}',
                            textAlign: TextAlign.center,
                            style: const TextStyle(
                                fontWeight: FontWeight.w600, fontSize: 14)),
                      ),
                      SizedBox(
                        width: 80,
                        child: Text('${d.totalQuantity.toInt()}',
                            textAlign: TextAlign.end,
                            style: const TextStyle(
                                fontWeight: FontWeight.bold, fontSize: 14)),
                      ),
                    ],
                  ),
                ),
              );
            },
          ),
        ),
      ],
    );
  }

  Widget _appBar(BuildContext context, ItemsProvider provider) {
    return Container(
      color: Colors.blue[700],
      padding: EdgeInsets.only(top: MediaQuery.of(context).padding.top),
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.arrow_back, color: Colors.white),
            onPressed: () => Navigator.of(context).pop(),
          ),
          const Expanded(
            child: Text('Items',
                style: TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.w500)),
          ),
          IconButton(
            icon: const Icon(Icons.search, color: Colors.white),
            onPressed: () => setState(() => _showSearch = !_showSearch),
          ),
          if (provider.categoryTab == 0)
            IconButton(
              icon: Icon(
                provider.viewMode == ItemViewMode.list
                    ? Icons.grid_view
                    : Icons.view_list,
                color: Colors.white,
              ),
              onPressed: provider.toggleView,
            ),
          ItemFilterPopup(provider: provider),
          IconButton(
            icon: const Icon(Icons.share, color: Colors.white),
            onPressed: () {
              showModalBottomSheet(
                context: context,
                shape: const RoundedRectangleBorder(
                  borderRadius:
                      BorderRadius.vertical(top: Radius.circular(20)),
                ),
                builder: (_) => const ShareBottomSheet(),
              );
            },
          ),
          IconButton(
            icon: const Icon(Icons.more_vert, color: Colors.white),
            onPressed: () async {
              final messenger = ScaffoldMessenger.of(context);
              final v = await showMenu<String>(
                context: context,
                position: RelativeRect.fromLTRB(1000, 80, 0, 0),
                items: const [
                  PopupMenuItem(value: 'import', child: Text('Import')),
                  PopupMenuItem(value: 'export', child: Text('Export')),
                  PopupMenuItem(value: 'refresh', child: Text('Refresh')),
                ],
              );
              if (v != null && context.mounted) {
                messenger.showSnackBar(SnackBar(content: Text('$v selected')));
              }
            },
          ),
        ],
      ),
    );
  }

  Widget _searchBar(ItemsProvider provider) {
    if (!_showSearch) return const SizedBox.shrink();
    return Container(
      color: Colors.blue[700],
      padding: const EdgeInsets.fromLTRB(16, 0, 16, 12),
      child: TextField(
        controller: _searchController,
        onChanged: provider.setSearchQuery,
        style: const TextStyle(color: Colors.white),
        decoration: InputDecoration(
          hintText: 'Search by name, category or group...',
          hintStyle: TextStyle(color: Colors.white60),
          prefixIcon: const Icon(Icons.search, color: Colors.white60),
          suffixIcon: _searchController.text.isNotEmpty
              ? IconButton(
                  icon: const Icon(Icons.clear, color: Colors.white60),
                  onPressed: () {
                    _searchController.clear();
                    provider.setSearchQuery('');
                  },
                )
              : null,
          filled: true,
          fillColor: Colors.white.withValues(alpha: 0.15),
          border: OutlineInputBorder(
              borderRadius: BorderRadius.circular(8), borderSide: BorderSide.none),
        ),
      ),
    );
  }

  Widget _summaryHeader(ItemsProvider provider) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(16),
      color: Colors.blue[50],
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(provider.summaryLabel,
               style: TextStyle(color: Colors.grey[800], fontSize: 13)),
          const SizedBox(height: 4),
          Text('${provider.summaryCount}',
              style:
                  const TextStyle(fontWeight: FontWeight.bold, fontSize: 24)),
        ],
      ),
    );
  }

  Widget _tabBar(ItemsProvider provider) {
    return Container(
      color: Colors.grey[100],
      child: Row(
        children: List.generate(provider.tabs.length, (i) {
          final selected = provider.categoryTab == i;
          return Expanded(
            child: GestureDetector(
              onTap: () {
                setState(() => _showSearch = false);
                _searchController.clear();
                provider.setCategoryTab(i);
              },
              child: Container(
                padding: const EdgeInsets.symmetric(vertical: 10),
                decoration: BoxDecoration(
                  color: selected ? Colors.white : Colors.transparent,
                  border: Border(
                    bottom: BorderSide(
                      color: selected ? Colors.blue : Colors.transparent,
                      width: 2,
                    ),
                  ),
                ),
                child: Text(
                  provider.tabs[i],
                  textAlign: TextAlign.center,
                  style: TextStyle(
                    fontWeight:
                        selected ? FontWeight.w600 : FontWeight.normal,
                    color: selected ? Colors.blue : Colors.grey,
                  ),
                ),
              ),
            ),
          );
        }),
      ),
    );
  }
}
