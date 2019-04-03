package report

import report.latex.LatexReportTemplate
import java.io.File


class ProgramCodeTemplate(val codeFilePath: File) : LatexReportTemplate() {

    override val preamble: String
        get() = super.preamble + """
\usepackage[dvipsnames]{xcolor}
\usepackage{verbatim}
\usepackage{listings}

\lstset{
columns=flexible,
breaklines=true,
extendedchars=\true,
inputencoding=utf8
}

\lstdefinelanguage{Kotlin}{
  comment=[l]{//},
  commentstyle={\color{gray}\ttfamily},
  emph={delegate, filter, first, firstOrNull, forEach, lazy, map, mapNotNull, println, return@},
  emphstyle={\color{OrangeRed}},
  identifierstyle=\color{black},
  keywords={abstract, actual, as, as?, break, by, class, companion, continue, data, do, dynamic, else, enum, expect, false, final, for, fun, get, if, import, in, interface, internal, is, null, object, override, package, private, public, return, set, super, suspend, this, throw, true, try, typealias, val, var, vararg, when, where, while, infix},
  keywordstyle={\color{NavyBlue}\bfseries},
  morecomment=[s]{/*}{*/},
  morestring=[b]",
  morestring=[s]{""${'"'}*}{*""${'"'}},
  ndkeywords={@Deprecated, @JvmField, @JvmName, @JvmOverloads, @JvmStatic, @JvmSynthetic, Array, Byte, Double, Float, Int, Integer, Iterable, Long, Runnable, Short, String},
  ndkeywordstyle={\color{BurntOrange}\bfseries},
  sensitive=true,
  showstringspaces=false,
  stringstyle={\color{ForestGreen}\ttfamily},
}
        """.trimIndent()

    override val documentBody: String
        get() = """
    \begin{center} ПРИЛОЖЕНИЕ Д \end{center}\\ \vspace{5mm}

    {
        \fontsize{8}{10}
        \setmainfont{Courier New}

        \lstinputlisting[language=Kotlin]{$codeFilePath}
    }
        """.trimIndent()
}