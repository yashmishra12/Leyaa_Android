# Leyaa Android v28.0.0 - Premium UI Upgrade & Modernization

This release focuses on delivering a highly polished, premium user experience while keeping full feature parity with our iOS counterpart. We've introduced several tactile micro-interactions, revamped our dark mode, and modernized the app's navigation and branding.

## 🌟 What's New

### 📳 Haptic Feedback & Interactivity
- **Tactile Responses**: Tapping on rooms, catalog items, or sending messages now triggers a satisfying, subtle haptic burst.
- **Swipe-to-Delete Dynamics**: When swiping away a shopping item, the trash icon dynamically scales up, making the interaction feel much more alive.

### 🎨 Visual & Layout Polish
- **Perfect Dark Mode (AMOLED)**: We overhauled our dark mode palette to use true AMOLED black (`#000000`). This not only delivers perfect contrast but also significantly saves battery life on OLED displays!
- **Glassmorphism Nav Bars**: The Top App Bars and Bottom Navigation Bars now feature a frosted glass (semi-transparent) effect, elegantly blurring your lists as they scroll underneath.
- **iMessage-Style Chat Bubbles**: We redesigned the Message Wall bubbles for a cleaner, flatter look with custom rounded "tails" to clearly distinguish sender from receiver.

### 🎉 Success Animations & Empty States
- **Satisfying "Bought" Action**: Swiping right to mark a shopping item as "Bought" now instantly triggers a large, vibrant green checkmark that smoothly pops up and fades away.
- **Interactive Empty Cart**: When a room's shopping list is empty, the cart icon now softly pulses, subtly encouraging you to add items.

### 🔄 Navigation Smoothness
- **Custom Screen Transitions**: Choppy screen switching is a thing of the past! We implemented smooth fade-in and fade-out animations globally across all bottom navigation tabs.
- **Optimized Intent Stacking**: Fixed an issue where switching tabs would sluggishly create new screens. We now use `REORDER_TO_FRONT` to ensure buttery-smooth navigation.

### 📱 Adaptive App Icon
- **Modern App Icon**: The app icon has been upgraded to a true Android Adaptive Icon! It now perfectly matches your device's preferred icon shape (circle, squircle, etc.).
- **Themed Icons (Android 13+)**: We added monochrome support! If you have "Themed Icons" enabled on your home screen, the Leyaa logo will beautifully tint itself to match your wallpaper and system colors.

### 🛠 Tech Debt & Under the Hood
- Migrated out of deprecated XML transitions in favor of properly managed Android Intents and Compose animations.
- Verified that Firebase Cloud Messaging pushes are utilizing the modern HTTP v1 API.

Enjoy the new, premium feel! Shop better. Live better.
