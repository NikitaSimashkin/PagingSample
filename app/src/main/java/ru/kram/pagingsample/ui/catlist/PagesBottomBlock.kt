import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme
import ru.kram.pagingsample.ui.catlist.model.PagesBlockData

@Composable
fun PagesBottomBlock(
    pagesBlockData: PagesBlockData,
    onPageClick: (page: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items((pagesBlockData.firstPage..pagesBlockData.lastPage).toList()) { page ->
            PageButton(
                number = page,
                isSelected = page == pagesBlockData.currentPage,
                onClick = onPageClick
            )
        }
    }
}

@Composable
private fun PageButton(
    number: Int,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (page: Int) -> Unit = {}
) {
    if (isSelected) {
        PrimaryActionButton(
            text = number.toString(),
            modifier = modifier.widthIn(min = 32.dp),
            onClick = { onClick(number) },
        )
    } else {
        SecondaryActionButton(
            text = number.toString(),
            modifier = modifier.widthIn(min = 32.dp),
            onClick = { onClick(number) },
        )
    }
}

@Preview
@Composable
private fun PageListPreview() {
    PagingSampleTheme {
        PagesBottomBlock(
            pagesBlockData = PagesBlockData(
                firstPage = 1,
                lastPage = 20,
                currentPage = 3
            ),
            onPageClick = {}
        )
    }
}