package au.edu.jcu.zhangfan.primoria.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Biotech
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MusicNote
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Translate
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.vector.ImageVector
import au.edu.jcu.zhangfan.primoria.ui.theme.PrimoriaTheme
import au.edu.jcu.zhangfan.primoria.ui.theme.SubjectColors

/**
 * Gives every topic — even one the learner just invented — a stable colour and a fitting glyph,
 * so generated content feels designed. The glyph means colour is never the only cue.
 */
object SubjectArt {

    /** Subject families: keywords → glyph and a palette slot, so colour also carries meaning. */
    private data class Family(val keywords: List<String>, val icon: ImageVector, val palette: Int)

    // Palette slots: 0 cobalt, 1 sage, 2 apricot, 3 plum, 4 rose, 5 cerulean, 6 ochre, 7 slate.
    private val families = listOf(
        Family(listOf("photosynth", "plant", "ecolog", "climate", "environment"), Icons.Outlined.Eco, 1),
        Family(listOf("cell", "biolog", "gene", "dna", "virus", "body"), Icons.Outlined.Biotech, 1),
        Family(listOf("chem", "atom", "volcan", "science", "electric"), Icons.Outlined.Science, 4),
        Family(listOf("force", "motion", "speed", "energy", "physics"), Icons.Outlined.Speed, 2),
        Family(listOf("water", "ocean", "rain", "river"), Icons.Outlined.WaterDrop, 5),
        Family(listOf("equation", "algebra", "math", "geometry", "fraction", "calcul"), Icons.Outlined.Calculate, 3),
        Family(listOf("probab", "statistic", "data"), Icons.Outlined.Calculate, 6),
        Family(listOf("algorithm", "program", "code", "computer", "python", "kotlin"), Icons.Outlined.Code, 0),
        Family(listOf("history", "war", "revolution", "empire", "ancient"), Icons.Outlined.AccountBalance, 6),
        Family(listOf("geograph", "country", "map", "continent"), Icons.Outlined.Public, 5),
        Family(listOf("english", "french", "spanish", "chinese", "grammar", "vocab"), Icons.Outlined.Translate, 4),
        Family(listOf("music", "song", "jazz"), Icons.Outlined.MusicNote, 3),
        Family(listOf("art", "paint", "design"), Icons.Outlined.Palette, 4),
        Family(listOf("psycholog", "mind", "brain", "philosoph"), Icons.Outlined.Psychology, 3),
        Family(listOf("literature", "poem", "novel", "shakespeare"), Icons.Outlined.AutoStories, 2),
        Family(listOf("economic", "politic", "society", "law"), Icons.Outlined.Language, 7),
    )

    private fun family(topic: String): Family? {
        val key = topic.lowercase()
        return families.firstOrNull { family -> family.keywords.any { it in key } }
    }

    fun icon(topic: String): ImageVector = family(topic)?.icon ?: Icons.Outlined.AutoStories

    /** FNV-1a so the same topic always gets the same colour across launches. */
    fun paletteIndex(topic: String, size: Int): Int {
        var hash = 0x811C9DC5.toInt()
        topic.trim().lowercase().forEach { char ->
            hash = hash xor char.code
            hash *= 0x01000193
        }
        return Math.floorMod(hash, size)
    }

    @Composable
    @ReadOnlyComposable
    fun colors(topic: String): SubjectColors {
        val palette = PrimoriaTheme.colors.subjects
        // Recognised subjects get their family colour; anything else gets a stable hashed colour.
        return palette[family(topic)?.palette ?: paletteIndex(topic, palette.size)]
    }
}
