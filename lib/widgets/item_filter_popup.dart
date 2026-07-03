import 'package:flutter/material.dart';
import 'package:bmstally_app/providers/items_provider.dart';

class ItemFilterPopup extends StatelessWidget {
  final ItemsProvider provider;

  const ItemFilterPopup({super.key, required this.provider});

  @override
  Widget build(BuildContext context) {
    return PopupMenuButton<ItemFilter>(
      icon: const Icon(Icons.filter_list),
      onSelected: provider.setFilter,
      itemBuilder: (_) => ItemFilter.values.map((f) {
        final selected = provider.filter == f;
        return PopupMenuItem(
          value: f,
          child: Row(
            children: [
              Icon(
                selected ? Icons.radio_button_checked : Icons.radio_button_unchecked,
                size: 20,
                color: selected ? Colors.blue : Colors.grey,
              ),
              const SizedBox(width: 12),
              Text(f.label),
            ],
          ),
        );
      }).toList(),
    );
  }
}
