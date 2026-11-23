package au.edu.jcu.zhangfan.primoria.ui.home

import androidx.lifecycle.viewModelScope
import au.edu.jcu.zhangfan.primoria.testing.FakeLearningRepository
import au.edu.jcu.zhangfan.primoria.testing.FakeProgressRepository
import au.edu.jcu.zhangfan.primoria.testing.FakeSettingsRepository
import au.edu.jcu.zhangfan.primoria.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val library = listOf("Photosynthesis", "Plate tectonics")
    private val settings = FakeSettingsRepository()
    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel(
            FakeLearningRepository(library = library),
            FakeProgressRepository(),
            settings,
        )
    }

    @After
    fun tearDown() {
        viewModel.viewModelScope.cancel()
    }

    @Test
    fun `consuming a topic trims collapses whitespace removes email and clears input`() {
        viewModel.onTopicInputChange("  Types\t of    volcanoes  learner@example.test  ")
        assertTrue(viewModel.canStart)

        val topic = viewModel.consumeTopic()

        assertEquals("Types of volcanoes", topic)
        assertEquals("", viewModel.topicInput)
        assertFalse(viewModel.canStart)
    }

    @Test
    fun `consuming blank input returns null`() {
        viewModel.onTopicInputChange(" \t\n ")

        val topic = viewModel.consumeTopic()

        assertNull(topic)
        assertFalse(viewModel.canStart)
    }

    @Test
    fun `topic input keeps the first 60 characters`() {
        val input = "Volcanoes ".repeat(6) + "extra"

        viewModel.onTopicInputChange(input)

        assertEquals(input.substring(0, 60), viewModel.topicInput)
    }

    @Test
    fun `ui state reflects consent updates and library titles`() = runTest {
        assertTrue(viewModel.uiState.value.loading)
        backgroundScope.launch { viewModel.uiState.collect() }
        runCurrent()
        val initialState = viewModel.uiState.value

        settings.update { it.copy(aiConsent = true) }
        runCurrent()

        assertFalse(initialState.loading)
        assertFalse(initialState.aiEnabled)
        assertEquals(library, initialState.libraryTopics)
        val updatedState = viewModel.uiState.value
        assertTrue(updatedState.aiEnabled)
        assertEquals(library, updatedState.libraryTopics)
    }
}
