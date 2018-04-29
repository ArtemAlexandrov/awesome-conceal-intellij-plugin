package sk.ca

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilder
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.util.TextRange
import java.util.*
import java.util.regex.Pattern

class CAFoldingBuilder : FoldingBuilder {
    private val symbolPattern = Pattern.compile(
            "function|->|<-|=>|===|!=|<=|>="
    )

    private val prettySymbolMaps = hashMapOf(
            "function" to "ƒ",
            "->" to "→",
            "<-" to "←",
            "=>" to "⇒",
            "===" to "≡",
            "!=" to "≠",
            "<=" to '≤',
            ">=" to '≥'
    )
    private val constants = arrayOf("function", "!=", "<=", ">=",
                                    "->", "=>", "<-", "===")

    override fun buildFoldRegions(node: ASTNode, document: Document): Array<out FoldingDescriptor> {
        val descriptors = ArrayList<FoldingDescriptor>()
        val text = node.text
        val matcher = symbolPattern.matcher(text)

        while (matcher.find()) {
            var key = text.substring(matcher.start(), matcher.end())
            val nodeRange = node.textRange
            var rangeStart = nodeRange.startOffset + matcher.start()
            var rangeEnd = nodeRange.startOffset + matcher.end()
            if (key.startsWith("(") && key != "()") {
                rangeStart += 1
            }
            if (rangeEnd + 1 > text.length) continue
            val nextChar = text.substring(rangeEnd, rangeEnd + 1)
            val prevChar = text.substring(rangeStart - 1, rangeStart)
            var shouldFold = constants.contains(key) && prevChar == " " && (nextChar == " " || nextChar == "\n")
            if (shouldFold) {
                val pretty = prettySymbolMaps[key] ?: return arrayOf<FoldingDescriptor>()
                val range = TextRange.create(rangeStart, rangeEnd)
                descriptors.add(CAFoldingDescriptor(node, range, null, pretty as String, true))
            }
        }
        return descriptors.toArray<FoldingDescriptor>(arrayOfNulls<FoldingDescriptor>(descriptors.size))
    }

    override fun getPlaceholderText(node: ASTNode) = null

    override fun isCollapsedByDefault(node: ASTNode) = true
}