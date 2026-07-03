import 'package:flutter/material.dart';

class CheckInReportScreen extends StatelessWidget {
  const CheckInReportScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        backgroundColor: Colors.blue[700],
        foregroundColor: Colors.white,
        title: const Text('Check In Report'),
      ),
      body: ListView.builder(
        padding: const EdgeInsets.all(16),
        itemCount: 6,
        itemBuilder: (_, i) {
          final names = ['Rahul Sharma', 'Priya Patel', 'Amit Singh', 'Sneha Reddy', 'Vikram Joshi', 'Neha Gupta'];
          final statuses = ['Checked In', 'Checked In', 'Late', 'Checked In', 'Absent', 'Checked In'];
          final times = ['09:15 AM', '09:30 AM', '10:45 AM', '09:05 AM', '-', '09:20 AM'];
          final locations = ['Site A - Andheri', 'Site B - Bandra', 'Site C - Powai', 'Site A - Andheri', '-', 'Site B - Bandra'];
          final s = statuses[i];
          final color = s == 'Checked In' ? Colors.green : s == 'Late' ? Colors.orange : Colors.red;
          final icon = s == 'Checked In' ? Icons.check_circle : s == 'Late' ? Icons.warning : Icons.cancel;

          return Card(
            margin: const EdgeInsets.symmetric(vertical: 5),
            elevation: 0,
            color: Colors.grey[50],
            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
            child: ListTile(
              leading: CircleAvatar(
                backgroundColor: color.withValues(alpha: 0.1),
                child: Icon(icon, color: color, size: 24),
              ),
              title: Text(names[i], style: const TextStyle(fontWeight: FontWeight.w600)),
              subtitle: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('$s | ${times[i]}', style: TextStyle(fontSize: 12, color: Colors.grey[700])),
                  Text(locations[i], style: TextStyle(fontSize: 11, color: Colors.grey[700])),
                ],
              ),
              isThreeLine: true,
            ),
          );
        },
      ),
    );
  }
}
