import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.kram.pagingsample.designsystem.button.PulseButton
import ru.kram.pagingsample.designsystem.theme.Colors
import ru.kram.pagingsample.designsystem.theme.PagingSampleTheme

@Composable
fun SecondaryActionButton(
    text: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SecondaryActionButton(
        modifier = modifier
            .height(48.dp),
        onClick = onClick,
        afterModifier = Modifier.padding(horizontal = 16.dp)
    ) {
        Text(
            text = text,
            color = Colors.textSecondary,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun SecondaryActionButton(
    vector: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SecondaryActionButton(
        modifier = modifier
            .size(40.dp),
        onClick = onClick
    ) {
        Icon(
            imageVector = vector,
            contentDescription = null,
            tint = Colors.textSecondary
        )
    }
}

@Composable
private fun SecondaryActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    afterModifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    PulseButton(
        onClick = onClick,
        modifier = modifier
            .background(
                color = Colors.secondary,
                shape = shape,
            )
            .border(
                width = 1.dp,
                color = Colors.primary,
                shape = shape,
            )
            .then(afterModifier),
        content
    )
}

@Preview
@Composable
private fun SecondaryActionButtonTextPreview() {
    PagingSampleTheme {
        SecondaryActionButton(text = "Click me", onClick = {})
    }
}

@Preview
@Composable
private fun SecondaryActionButtonIconPreview() {
    PagingSampleTheme {
        SecondaryActionButton(
            vector = Icons.Default.Add,
            onClick = {}
        )
    }
}