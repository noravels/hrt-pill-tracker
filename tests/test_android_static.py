import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
ANDROID = ROOT / "src" / "android"


def read(path: Path) -> str:
    return path.read_text(encoding="utf-8")


class AndroidStaticTest(unittest.TestCase):
    def test_android_module_is_registered_and_has_compose_entrypoint(self):
        settings = read(ROOT / "settings.gradle.kts")
        self.assertIn('include(":android")', settings)
        self.assertIn('project(":android").projectDir = file("src/android")', settings)

        build = read(ANDROID / "build.gradle.kts")
        self.assertIn('com.android.application', build)
        self.assertIn('org.jetbrains.kotlin.android', build)
        self.assertIn('org.jetbrains.kotlin.plugin.compose', build)

        main_activity = read(ANDROID / "src" / "main" / "java" / "app" / "transition" / "hrt" / "android" / "MainActivity.kt")
        self.assertIn("class MainActivity", main_activity)
        self.assertIn("setContent", main_activity)
        self.assertIn("HrtTrackerApp", main_activity)

    def test_android_resources_include_english_and_turkish_without_phase_copy(self):
        en = read(ANDROID / "src" / "main" / "res" / "values" / "strings.xml")
        tr = read(ANDROID / "src" / "main" / "res" / "values-tr" / "strings.xml")

        required_names = [
            "app_name",
            "today_title",
            "local_only_label",
            "next_dose_label",
            "mark_dose_taken",
            "active_routine_title",
            "next_24_hours_title",
            "setup_title",
            "routine_title",
            "save_local_routine",
            "taken_toast",
        ]
        for name in required_names:
            self.assertIn(f'name="{name}"', en)
            self.assertIn(f'name="{name}"', tr)

        all_copy = (en + "\n" + tr).lower()
        banned = ["phase", "faz", "mvp", "implementation", "implementasyon"]
        for word in banned:
            self.assertNotIn(word, all_copy)

    def test_android_design_uses_007_structural_tokens_not_gradient_skin(self):
        source = read(ANDROID / "src" / "main" / "java" / "app" / "transition" / "hrt" / "android" / "MainActivity.kt")

        self.assertIn("TransBlue", source)
        self.assertIn("TransPink", source)
        self.assertIn("Color(0xFF5BCEFA)", source)
        self.assertIn("Color(0xFFF5A9B8)", source)
        self.assertIn("SideIdentityRail", source)
        self.assertIn("DoseIdentityRail", source)
        self.assertNotIn("Brush.linearGradient", source)
        self.assertNotIn("Brush.verticalGradient", source)
        self.assertNotIn("Brush.horizontalGradient", source)

    def test_user_visible_badges_and_navigation_copy_are_localized(self):
        source = read(ANDROID / "src" / "main" / "java" / "app" / "transition" / "hrt" / "android" / "MainActivity.kt")
        hardcoded_user_copy = ['"Fri"', '"Any"', '"opt"', '"now"', '"next"', '"on"', '"log"', '"20:42"']
        for literal in hardcoded_user_copy:
            self.assertNotIn(literal, source)

        en = read(ANDROID / "src" / "main" / "res" / "values" / "strings.xml")
        tr = read(ANDROID / "src" / "main" / "res" / "values-tr" / "strings.xml")
        for name in ["weekday_friday", "any_time_label", "badge_optional", "badge_now", "badge_next", "badge_on", "badge_log", "back_action"]:
            self.assertIn(f'name="{name}"', en)
            self.assertIn(f'name="{name}"', tr)


if __name__ == "__main__":
    unittest.main()
