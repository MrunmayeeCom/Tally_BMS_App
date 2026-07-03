import 'package:flutter/material.dart';

class ActionCard extends StatelessWidget {
  final IconData icon;
  final String title;
  final String? description;
  final String? buttonLabel;
  final bool showArrow;
  final VoidCallback? onTap;
  final VoidCallback? onButtonTap;

  const ActionCard({
    super.key,
    required this.icon,
    required this.title,
    this.description,
    this.buttonLabel,
    this.showArrow = false,
    this.onTap,
    this.onButtonTap,
  });

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        borderRadius: BorderRadius.circular(12),
        onTap: onTap,
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  Icon(icon, size: 28, color: Theme.of(context).colorScheme.primary),
                  const SizedBox(width: 12),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(title, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 16)),
                        if (description != null) ...[
                          const SizedBox(height: 4),
                          Text(description!, style: TextStyle(color: Colors.grey[700], fontSize: 13)),
                        ],
                      ],
                    ),
                  ),
                  if (showArrow)
                    const Icon(Icons.chevron_right, color: Colors.grey),
                ],
              ),
              if (buttonLabel != null) ...[
                const SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  child: FilledButton(
                    onPressed: onButtonTap ?? () {},
                    child: Text(buttonLabel!),
                  ),
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
