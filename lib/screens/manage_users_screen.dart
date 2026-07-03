import 'package:flutter/material.dart';

class ManageUsersScreen extends StatefulWidget {
  const ManageUsersScreen({super.key});

  @override
  State<ManageUsersScreen> createState() => _ManageUsersScreenState();
}

class _ManageUsersScreenState extends State<ManageUsersScreen> {
  final List<Map<String, Object?>> _users = [
    {'name': 'Rahul Sharma', 'role': 'Sales Executive', 'active': true},
    {'name': 'Priya Patel', 'role': 'Sales Executive', 'active': true},
    {'name': 'Amit Singh', 'role': 'Field Agent', 'active': true},
    {'name': 'Sneha Reddy', 'role': 'Sales Executive', 'active': false},
    {'name': 'Vikram Joshi', 'role': 'Field Agent', 'active': true},
    {'name': 'Neha Gupta', 'role': 'Manager', 'active': true},
  ];

  String _str(Map<String, Object?> map, String key) => (map[key] as String?) ?? '';
  bool _bool(Map<String, Object?> map, String key) => (map[key] as bool?) ?? false;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Manage Users'),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: _users.length + 1,
        itemBuilder: (_, i) {
          if (i == _users.length) {
            return Padding(
              padding: const EdgeInsets.only(top: 16),
              child: SizedBox(
                width: double.infinity,
                height: 48,
                child: OutlinedButton.icon(
                  icon: const Icon(Icons.add),
                  label: const Text('Add New User'),
                  onPressed: () => ScaffoldMessenger.of(context).showSnackBar(
                    const SnackBar(content: Text('Add user form')),
                  ),
                ),
              ),
            );
          }
          final u = _users[i];
          final name = _str(u, 'name');
          final role = _str(u, 'role');
          final active = _bool(u, 'active');
          return Card(
            margin: const EdgeInsets.symmetric(vertical: 4),
            elevation: 0,
            color: Colors.grey[50],
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: Colors.indigo.withValues(alpha: 0.1),
                child: Text(name[0], style: const TextStyle(color: Colors.indigo, fontWeight: FontWeight.bold)),
              ),
              title: Text(name, style: const TextStyle(fontWeight: FontWeight.w600)),
              subtitle: Text(role, style: TextStyle(fontSize: 12, color: Colors.grey[700])),
              trailing: active
                  ? Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: Colors.green.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: const Text('Active', style: TextStyle(fontSize: 11, color: Colors.green, fontWeight: FontWeight.w600)),
                    )
                  : Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 3),
                      decoration: BoxDecoration(
                        color: Colors.grey.withValues(alpha: 0.2),
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Text('Inactive', style: TextStyle(fontSize: 11, color: Colors.grey[700], fontWeight: FontWeight.w600)),
                    ),
            ),
          );
        },
      ),
    );
  }
}
