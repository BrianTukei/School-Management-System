package com.example

import android.app.Application
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.navigation.compose.rememberNavController
import androidx.test.core.app.ApplicationProvider
import com.example.ui.SchoolViewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SchoolAppCrashTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var application: Application
    private lateinit var viewModel: SchoolViewModel

    @Before
    fun setup() {
        application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = SchoolViewModel(application)
    }

    @Test
    fun test_render_school_app_does_not_crash() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SchoolApp(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_student_detail_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                StudentDetailScreen(studentId = 1, navController = navController, viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_student_form_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                StudentFormScreen(studentId = 0, navController = navController, viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_teachers_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                TeachersListScreen(navController = navController, viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_teacher_form_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                TeacherFormScreen(teacherId = 0, navController = navController, viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_classes_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                ClassesScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_attendance_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AttendanceScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_grades_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                GradesScreen(navController = navController, viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_ai_assistant_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AiAssistantScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_sms_broadcast_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                SmsBroadcastScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_pupil_monitoring_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                PupilMonitoringScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_render_timetable_planner_screen() {
        composeTestRule.setContent {
            MyApplicationTheme {
                TimetablePlannerScreen(viewModel = viewModel)
            }
        }
        composeTestRule.waitForIdle()
        composeTestRule.onRoot().assertExists()
    }

    @Test
    fun test_timetable_overlap_detection() {
        // Overlapping ranges
        org.junit.Assert.assertTrue(com.example.ui.screens.timesOverlap("08:30 AM", "09:30 AM", "09:00 AM", "10:00 AM"))
        org.junit.Assert.assertTrue(com.example.ui.screens.timesOverlap("12:00 PM", "01:00 PM", "11:00 AM", "12:30 PM"))
        
        // Exact same range
        org.junit.Assert.assertTrue(com.example.ui.screens.timesOverlap("11:00 AM", "12:00 PM", "11:00 AM", "12:00 PM"))

        // Adjacent ranges (not overlapping)
        org.junit.Assert.assertFalse(com.example.ui.screens.timesOverlap("08:30 AM", "09:30 AM", "09:30 AM", "10:30 AM"))
        org.junit.Assert.assertFalse(com.example.ui.screens.timesOverlap("08:30 AM", "09:30 AM", "09:45 AM", "11:00 AM"))
        org.junit.Assert.assertFalse(com.example.ui.screens.timesOverlap("12:00 PM", "01:00 PM", "01:00 PM", "02:00 PM"))
    }
}
