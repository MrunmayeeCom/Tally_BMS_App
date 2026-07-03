import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/items_provider.dart';
import 'package:bmstally_app/widgets/item_card.dart';

class ItemGridView extends StatelessWidget {
  const ItemGridView({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<ItemsProvider>();
    final items = provider.items;

    if (items.isEmpty) {
      return const Center(child: Text('No items found'));
    }

    return GridView.builder(
      padding: const EdgeInsets.all(16),
      gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
        crossAxisCount: 2,
        crossAxisSpacing: 12,
        mainAxisSpacing: 12,
        childAspectRatio: 0.9,
      ),
      itemCount: items.length,
      itemBuilder: (_, i) => ItemCard(item: items[i]),
    );
  }
}
