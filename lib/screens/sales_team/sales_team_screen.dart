import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';
import 'package:bmstally_app/widgets/action_card.dart';
import 'package:bmstally_app/screens/entry_screen.dart';
import 'package:bmstally_app/screens/check_in_report_screen.dart';
import 'package:bmstally_app/screens/follow_ups_screen.dart';
import 'package:bmstally_app/screens/manage_users_screen.dart';

class SalesTeamScreen extends StatelessWidget {
  const SalesTeamScreen({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppProvider>();
    final entries = provider.salesEntries;

    return Column(
      children: [
        _appBar(context),
        Expanded(
          child: ListView(
            padding: const EdgeInsets.symmetric(vertical: 8),
            children: [
              ActionCard(
                icon: Icons.receipt_long,
                title: entries[0].title,
                description: entries[0].description,
                buttonLabel: entries[0].actionLabel,
                onButtonTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const EntryScreen()),
                  );
                },
              ),
              ActionCard(
                icon: Icons.check_circle_outline,
                title: entries[1].title,
                description: entries[1].description,
                buttonLabel: entries[1].actionLabel,
                showArrow: entries[1].hasArrow,
                onButtonTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const CheckInReportScreen()),
                  );
                },
              ),
              ActionCard(
                icon: Icons.alarm,
                title: entries[2].title,
                description: entries[2].description,
                buttonLabel: entries[2].actionLabel,
                showArrow: entries[2].hasArrow,
                onButtonTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const FollowUpsScreen()),
                  );
                },
              ),
              ActionCard(
                icon: Icons.people_outline,
                title: entries[3].title,
                description: entries[3].description,
                showArrow: entries[3].hasArrow,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(builder: (_) => const ManageUsersScreen()),
                  );
                },
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _appBar(BuildContext context) {
    return Container(
      color: Colors.blue[700],
      padding: EdgeInsets.only(top: MediaQuery.of(context).padding.top),
      child: Row(
        children: [
          IconButton(
            icon: const Icon(Icons.menu, color: Colors.white),
            onPressed: () => Scaffold.of(context).openDrawer(),
          ),
          const Expanded(
            child: Text('Sales Team',
                style: TextStyle(
                    color: Colors.white,
                    fontSize: 18,
                    fontWeight: FontWeight.w500)),
          ),
        ],
      ),
    );
  }
}
