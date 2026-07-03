import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:bmstally_app/providers/app_provider.dart';

class CustomDrawer extends StatelessWidget {
  const CustomDrawer({super.key});

  @override
  Widget build(BuildContext context) {
    final provider = context.watch<AppProvider>();
    final theme = Theme.of(context);

    return Drawer(
      child: ListView(
        padding: EdgeInsets.zero,
        children: [
          DrawerHeader(
            decoration: BoxDecoration(color: theme.colorScheme.primary),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    CircleAvatar(
                      backgroundColor: Colors.white,
                      radius: 24,
                      child: Text(
                        provider.userName.isNotEmpty
                            ? provider.userName[0].toUpperCase()
                            : 'U',
                        style: TextStyle(
                          color: theme.colorScheme.primary,
                          fontWeight: FontWeight.bold,
                          fontSize: 18,
                        ),
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(provider.userName,
                              style: const TextStyle(color: Colors.white, fontWeight: FontWeight.bold, fontSize: 16)),
                          Text(provider.userEmail,
                               style: const TextStyle(color: Colors.white70, fontSize: 12)),
                          const SizedBox(height: 2),
                          Text(provider.currentTenant?.name ?? '',
                               style: const TextStyle(color: Colors.white60, fontSize: 11)),
                        ],
                      ),
                    ),
                    const Icon(Icons.edit, color: Colors.white, size: 20),
                  ],
                ),
                const Spacer(),
                Text('Expires on: 24 Jun 26',
                    style: TextStyle(color: Colors.white70, fontSize: 12)),
              ],
            ),
          ),
          _section('MY ACCOUNT'),
          _item(context, Icons.business, 'Companies', () => _open(context, '/companies')),
          _item(context, Icons.group, 'Users', () => _open(context, '/users'), showDot: true),
          _item(context, Icons.settings, 'Settings', () => _open(context, '/settings')),
          _item(context, Icons.people, 'Refer a Friend', () {
            Navigator.of(context).pop();
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Refer a Friend - Share your referral code')),
            );
          }),
          _item(context, Icons.account_balance_wallet, 'Wallet', () => _open(context, '/wallet'),
              trailing: Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: Colors.green,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Text('300 coins', style: TextStyle(color: Colors.white, fontSize: 11)),
              )),
          const Divider(),
          _section('SUBSCRIPTION'),
          _item(context, Icons.card_membership, 'Purchase Subscription', () => _open(context, '/subscription')),
          const Divider(),
          _section('SECURITY'),
          _item(context, Icons.lock_outline, 'Set Passcode', () {
            Navigator.of(context).pop();
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Set Passcode feature')),
            );
          }),
          _item(context, Icons.lock, 'Forgot Password', () {
            Navigator.of(context).pop();
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(content: Text('Forgot Password - Reset link will be sent')),
            );
          }),
          const Divider(),
          _section('SUPPORT'),
          _item(context, Icons.info_outline, 'Version 19.6.2', () {
            Navigator.of(context).pop();
            showAboutDialog(
              context: context,
              applicationName: 'BMS Tally',
              applicationVersion: '19.6.2',
            );
          }),
          _item(context, Icons.help_outline, 'Help', () => _open(context, '/help')),
          _item(context, Icons.info, 'About', () => _open(context, '/about')),
          const Divider(),
          _item(context, Icons.power_settings_new, 'Logout', () {
            Navigator.of(context).pop();
            provider.logout();
            Navigator.of(context).pushReplacementNamed('/login');
          }),
        ],
      ),
    );
  }

  void _open(BuildContext context, String route) {
    Navigator.of(context).pop();
    Navigator.of(context).pushNamed(route);
  }

  Widget _section(String title) {
    return Padding(
      padding: const EdgeInsets.fromLTRB(16, 12, 16, 4),
      child: Text(title,
          style: const TextStyle(fontWeight: FontWeight.w600, color: Colors.grey, fontSize: 13)),
    );
  }

  Widget _item(BuildContext context, IconData icon, String label, VoidCallback onTap,
      {bool showDot = false, Widget? trailing}) {
    return ListTile(
      leading: Stack(
        clipBehavior: Clip.none,
        children: [
          Icon(icon),
          if (showDot)
            Positioned(
              right: -4,
              top: -4,
              child: Container(
                width: 8,
                height: 8,
                decoration: const BoxDecoration(color: Colors.orange, shape: BoxShape.circle),
              ),
            ),
        ],
      ),
      title: Text(label, style: const TextStyle(fontSize: 14)),
      trailing: trailing,
      onTap: onTap,
      dense: true,
    );
  }
}
