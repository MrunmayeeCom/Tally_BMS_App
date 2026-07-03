import 'package:flutter_test/flutter_test.dart';
import 'package:bmstally_app/main.dart';

void main() {
  testWidgets('Login screen renders', (WidgetTester tester) async {
    await tester.pumpWidget(const BmsTallyApp());
    await tester.pumpAndSettle();

    expect(find.text('BMS Tally'), findsOneWidget);
    expect(find.text('Login'), findsOneWidget);
  });
}
