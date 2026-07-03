import 'package:flutter/material.dart';
import 'package:bmstally_app/screens/transaction_module_screen.dart';

class _EntryType {
  final String title;
  final IconData icon;
  final String description;
  final bool isNew;

  const _EntryType({
    required this.title,
    required this.icon,
    required this.description,
    this.isNew = false,
  });
}

const _entryTypes = [
  _EntryType(title: 'Sales Invoice', icon: Icons.receipt_long, description: 'Create and manage sales invoices', isNew: true),
  _EntryType(title: 'Sales Order', icon: Icons.shopping_bag, description: 'Record customer sales orders'),
  _EntryType(title: 'Receipt', icon: Icons.account_balance_wallet, description: 'Record payments received'),
  _EntryType(title: 'Delivery Note', icon: Icons.local_shipping, description: 'Track goods delivery'),
  _EntryType(title: 'Quotation', icon: Icons.description, description: 'Create price quotations for customers', isNew: true),
  _EntryType(title: 'Purchase Invoice', icon: Icons.shopping_cart, description: 'Record purchases from suppliers'),
  _EntryType(title: 'Purchase Order', icon: Icons.assignment, description: 'Create purchase orders'),
  _EntryType(title: 'Payment', icon: Icons.money_off, description: 'Record payments made'),
  _EntryType(title: 'Receipt Note', icon: Icons.note_add, description: 'Track goods received'),
  _EntryType(title: 'Journal', icon: Icons.book, description: 'Record journal entries'),
  _EntryType(title: 'Ledger', icon: Icons.account_balance, description: 'View and manage ledgers'),
];

class EntryScreen extends StatelessWidget {
  const EntryScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Entry'),
        actions: [
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Entry settings')),
            ),
          ),
        ],
      ),
      body: ListView.separated(
        padding: const EdgeInsets.symmetric(vertical: 8),
        itemCount: _entryTypes.length,
        separatorBuilder: (_, _) => const Divider(height: 1, indent: 16, endIndent: 16),
        itemBuilder: (_, i) {
          final e = _entryTypes[i];
          return ListTile(
            leading: CircleAvatar(
              backgroundColor: Colors.blue.withValues(alpha: 0.1),
              child: Icon(e.icon, color: Colors.blue[700], size: 22),
            ),
            title: Row(
              children: [
                Text(e.title, style: const TextStyle(fontWeight: FontWeight.w500, fontSize: 15)),
                if (e.isNew) ...[
                  const SizedBox(width: 8),
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                    decoration: BoxDecoration(
                      color: Colors.green,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: const Text('New', style: TextStyle(color: Colors.white, fontSize: 10, fontWeight: FontWeight.w600)),
                  ),
                ],
              ],
            ),
            subtitle: Text(e.description, style: TextStyle(fontSize: 12, color: Colors.grey[700])),
            trailing: const Icon(Icons.chevron_right, color: Colors.grey, size: 20),
            onTap: () {
              Navigator.of(context).push(
                MaterialPageRoute(
                  builder: (_) => TransactionModuleScreen(title: e.title),
                ),
              );
            },
          );
        },
      ),
    );
  }
}
