import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/models/item.dart';
import 'package:bmstally_app/providers/items_provider.dart';

class CreateItemScreen extends StatefulWidget {
  const CreateItemScreen({super.key});

  @override
  State<CreateItemScreen> createState() => _CreateItemScreenState();
}

class _CreateItemScreenState extends State<CreateItemScreen> {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  final _descController = TextEditingController();
  final _openingStockController = TextEditingController();
  final _purchaseRateController = TextEditingController();
  final _salesRateController = TextEditingController();

  String? _category;
  String? _group;
  String? _unit;

  @override
  void dispose() {
    _nameController.dispose();
    _descController.dispose();
    _openingStockController.dispose();
    _purchaseRateController.dispose();
    _salesRateController.dispose();
    super.dispose();
  }

  void _save() {
    if (!_formKey.currentState!.validate()) return;
    if (_category == null || _unit == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please fill all required fields')),
      );
      return;
    }

    final provider = context.read<ItemsProvider>();
    final item = Item(
      id: DateTime.now().millisecondsSinceEpoch.toString(),
      name: _nameController.text.trim(),
      category: _category!,
      group: _group ?? '',
      unit: _unit!,
      openingStock: double.tryParse(_openingStockController.text) ?? 0,
      quantity: double.tryParse(_openingStockController.text) ?? 0,
      purchaseRate: double.tryParse(_purchaseRateController.text) ?? 0,
      salesRate: double.tryParse(_salesRateController.text) ?? 0,
      description: _descController.text.trim(),
    );
    provider.addItem(item);
    Navigator.of(context).pop();
    ScaffoldMessenger.of(context).showSnackBar(
      const SnackBar(content: Text('Item saved successfully')),
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Create Item'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(24),
        child: Form(
          key: _formKey,
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('Item Information',
                  style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
              const SizedBox(height: 16),
              TextFormField(
                controller: _nameController,
                decoration: const InputDecoration(
                  labelText: 'Item Name *',
                  prefixIcon: Icon(Icons.inventory_2),
                  border: OutlineInputBorder(),
                ),
                validator: (v) => v == null || v.trim().isEmpty ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _category,
                decoration: const InputDecoration(
                  labelText: 'Category *',
                  prefixIcon: Icon(Icons.category),
                  border: OutlineInputBorder(),
                ),
                items: ['Building Material', 'Paint', 'Plumbing', 'Tiles', 'Electrical', 'Sanitary']
                    .map((c) => DropdownMenuItem(value: c, child: Text(c)))
                    .toList(),
                onChanged: (v) => setState(() => _category = v),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _group,
                decoration: const InputDecoration(
                  labelText: 'Group',
                  prefixIcon: Icon(Icons.group_work),
                  border: OutlineInputBorder(),
                ),
                items: ['Raw Material', 'Finished Good', 'Trading Good', 'Consumable']
                    .map((g) => DropdownMenuItem(value: g, child: Text(g)))
                    .toList(),
                onChanged: (v) => setState(() => _group = v),
              ),
              const SizedBox(height: 16),
              DropdownButtonFormField<String>(
                initialValue: _unit,
                decoration: const InputDecoration(
                  labelText: 'Unit *',
                  prefixIcon: Icon(Icons.scale),
                  border: OutlineInputBorder(),
                ),
                items: ['Nos', 'Kg', 'Pcs', 'Bags', 'Box', 'Roll', 'Ton', 'Set', 'Litre']
                    .map((u) => DropdownMenuItem(value: u, child: Text(u)))
                    .toList(),
                onChanged: (v) => setState(() => _unit = v),
                validator: (v) => v == null ? 'Required' : null,
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _openingStockController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Opening Stock',
                  prefixIcon: Icon(Icons.storage),
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _purchaseRateController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Purchase Rate',
                  prefixIcon: Icon(Icons.shopping_cart),
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _salesRateController,
                keyboardType: TextInputType.number,
                decoration: const InputDecoration(
                  labelText: 'Sales Rate',
                  prefixIcon: Icon(Icons.attach_money),
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 16),
              TextFormField(
                controller: _descController,
                maxLines: 3,
                decoration: const InputDecoration(
                  labelText: 'Description',
                  prefixIcon: Icon(Icons.description),
                  border: OutlineInputBorder(),
                ),
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: FilledButton(
                  onPressed: _save,
                  child: const Text('Save Item'),
                ),
              ),
              const SizedBox(height: 12),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: OutlinedButton(
                  onPressed: () => Navigator.of(context).pop(),
                  child: const Text('Cancel'),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
