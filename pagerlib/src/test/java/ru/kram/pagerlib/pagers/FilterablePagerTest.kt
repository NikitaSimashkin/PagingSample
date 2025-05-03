package ru.kram.pagerlib.pagers

import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import ru.kram.pagerlib.utils.FakeIntDataSource

@OptIn(ExperimentalCoroutinesApi::class)
class FilterablePagerTest {

    private lateinit var pager: FilterablePager<Int>
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        pager = FilterablePager(
            primaryDataSource = FakeIntDataSource(),
            initialPage = INITIAL_PAGE,
            pageSize = PAGE_SIZE,
            threshold = THRESHOLD,
            maxItemsToKeep = 100,
            minItemsToLoad = 1
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialPageLoadsCorrectly() = runTest {
        pager.data.test {
            pager.onItemVisible(null)
            skipItems(1)

            assertEquals(getExpectedPage(INITIAL_PAGE), awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
        pager.destroy()
    }

    @Test
    fun testScrollBeyondThresholdLoadsNextPage() = runTest {
        pager.data.test {
            pager.onItemVisible(null)
            skipItems(1)
            assertEquals(getExpectedPage(INITIAL_PAGE), awaitItem())

            pager.onItemVisible(11)
            val expectedPages = getExpectedPage(INITIAL_PAGE) + getExpectedPage(INITIAL_PAGE + 1)
            assertEquals(expectedPages, awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
        pager.destroy()
    }

    private fun getExpectedPage(page: Int): List<Int> {
        val start = (page - 1) * PAGE_SIZE + 1
        return (start until start + PAGE_SIZE).toList()
    }

    companion object {
        private const val INITIAL_PAGE = 1
        private const val PAGE_SIZE = 10
        private const val THRESHOLD = 3
    }
}