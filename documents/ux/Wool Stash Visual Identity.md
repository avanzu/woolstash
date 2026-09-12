# Wool Stash Companion – Visual Identity Briefing

Use the attached visual board as the primary design reference.

## Core design principle

The app has two visual layers:

1. **Brand identity**
   - launcher icon
   - splash screen
   - yarn and spinning-fiber product icons
   - branded illustrations and decorative elements

2. **Application UI**
   - navigation
   - buttons
   - forms
   - dialogs
   - lists
   - cards
   - search
   - filters
   - standard controls

The application UI should remain recognizably Android and should use Jetpack Compose / Material 3 conventions.

Do **not** heavily customize standard Android controls just to make them look branded.

The brand identity should live mainly in the artwork, product icons, splash screen, selected accent colors, and occasional decorative elements.

## Theme priority

The user's Android device is normally configured for dark mode.

Therefore:

- design **dark-first**
- light mode must remain usable and attractive
- do not treat dark mode as merely an inverted light theme
- branded assets themselves should not depend on the Android theme

## Fixed brand palette

The brand artwork uses its own fixed colors rather than deriving everything from MaterialTheme.

Approximate direction:

- background: very dark aubergine / plum
- yarn: berry / mauve / raspberry
- spinning fiber: teal / turquoise / petrol
- text / light accent: warm cream / off-white

Exact values may be refined during implementation.

The important semantic distinction is:

- **Yarn = berry/violet family**
- **Spinning fiber = teal/petrol family**

These colors should remain clearly distinguishable.

## Product icons

### Yarn

Represent yarn as a compact round yarn ball.

Important:
- visibly three-dimensional
- soft textile appearance
- rounded strands
- subtle highlights and shadows
- no flat line-art appearance
- readable at small sizes

### Spinning fiber

Represent unspun fiber as a long fiber strand stored in a loose knot.

Important:
- not braided
- not a plait
- not a rope
- clearly softer and less tightly structured than yarn
- elongated silhouette
- visibly three-dimensional
- same rendering style and material treatment as the yarn icon

The two icons must remain recognizable even without color.

Their silhouettes should contrast:

- yarn: compact / round / wound
- fiber: elongated / loose / knotted

## Relationship between brand assets and Material 3

Brand assets should **not** automatically change color between light and dark themes.

For example:
- the yarn icon stays berry
- the fiber icon stays teal
- the splash artwork keeps its aubergine-based palette

Material 3 theming applies mainly to the surrounding application UI.

When product icons appear inside ordinary UI, place them on suitable containers if needed rather than recoloring the artwork to match the current theme.

## Splash screen

The splash screen is a branding surface, not a generic Android background.

Use:
- fixed dark aubergine/plum background
- yarn artwork
- Wool Stash Companion title
- optional restrained fiber/yarn decorative curves

Avoid:
- generic black background
- generic white background
- theme-derived splash colors
- excessive animation

## Launcher icon

The launcher icon should use the same visual language as the product icons:

- three-dimensional yarn
- berry/violet palette
- dark aubergine background
- optional loose yarn tail / heart motif

It should still work at very small launcher sizes.

## General mood

The visual identity should feel:

- warm
- tactile
- calm
- personal
- handcrafted
- slightly playful
- polished, not childish

Avoid:
- warehouse/inventory aesthetics
- ERP-like symbolism
- generic database imagery
- excessive skeuomorphism
- flat corporate iconography
- neon colors

The product concept is **Companion rather than Inventory Control**.

The app should feel like a personal wool stash companion, while its actual controls should still behave and look like a modern Android app.

## Implementation guidance

Prefer:
- Jetpack Compose
- Material 3 components
- standard Android interaction patterns
- brand assets as PNG/WebP/vector assets where appropriate
- fixed asset colors
- normal theme-aware surfaces and controls around those assets

Do not attempt to recreate the 3D yarn/fiber artwork procedurally in Compose unless there is a strong technical reason.

Treat the attached board as a visual direction, not as a pixel-perfect screen specification.