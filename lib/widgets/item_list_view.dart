import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/items_provider.dart';
import 'package:bmstally_app/widgets/item_tile.dart';

class ItemListView extends StatelessWidget {
  const ItemListView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ItemsProvider>();
    final items = provider.items;

    return Column(
      children: [
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
          color: Colors.grey[100],
          child: Row(
            children: [
              Text('Item Name',
                   style: TextStyle(fontWeight: FontWeight.w600, fontSize: 12, color: Colors.grey[800])),
              const Spacer(),
              Text('Qty',
                   style: TextStyle(fontWeight: FontWeight.w600, fontSize: 12, color: Colors.grey[800])),
            ],
          ),
        ),
        Expanded(
          child: items.isEmpty
              ? const Center(child: Text('No items found'))
              : ListView.builder(
                  itemCount: items.length,
                  itemBuilder: (_, i) => ItemTile(item: items[i]),
                ),
        ),
      ],
    );
  }
}
