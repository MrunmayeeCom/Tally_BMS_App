import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';

class CreateEntryScreen extends StatefulWidget {
  const CreateEntryScreen({super.key});

  @override
  State<CreateEntryScreen> createState() => _CreateEntryScreenState();
}

class _CreateEntryScreenState extends State<CreateEntryScreen> {
  String? _entryType;
  String? _party;

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppProvider>();

    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Create New'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Entry Details',
                style: TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              initialValue: _entryType,
              decoration: const InputDecoration(
                labelText: 'Entry Type',
                prefixIcon: Icon(Icons.description_outlined),
                border: OutlineInputBorder(),
              ),
              items: provider.entryTypes
                  .map((t) => DropdownMenuItem(value: t, child: Text(t)))
                  .toList(),
              onChanged: (v) => setState(() => _entryType = v),
            ),
            const SizedBox(height: 16),
            DropdownButtonFormField<String>(
              initialValue: _party,
              decoration: const InputDecoration(
                labelText: 'Party / Customer',
                prefixIcon: Icon(Icons.person_outline),
                border: OutlineInputBorder(),
              ),
              items: provider.parties
                  .map((p) => DropdownMenuItem(value: p, child: Text(p)))
                  .toList(),
              onChanged: (v) => setState(() => _party = v),
            ),
            const Spacer(),
            SizedBox(
              width: double.infinity,
              height: 48,
              child: FilledButton(
                onPressed:
                    _entryType != null && _party != null ? () {
                      ScaffoldMessenger.of(context).showSnackBar(
                        SnackBar(content: Text('$_entryType created for $_party')),
                      );
                      Navigator.of(context).pop();
                    } : null,
                child: const Text('CREATE'),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
