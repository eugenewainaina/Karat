package com.alex.Karat.styles

import com.varabyte.kobweb.compose.css.CSSTransition
import com.varabyte.kobweb.compose.css.Cursor
import com.varabyte.kobweb.compose.ui.Modifier
import com.varabyte.kobweb.compose.ui.graphics.Colors
import com.varabyte.kobweb.compose.ui.modifiers.*
import com.varabyte.kobweb.silk.components.style.ComponentStyle
import com.varabyte.kobweb.silk.components.style.disabled
import com.varabyte.kobweb.silk.components.style.hover
import org.jetbrains.compose.web.css.Color
import org.jetbrains.compose.web.css.LineStyle
import org.jetbrains.compose.web.css.ms
import org.jetbrains.compose.web.css.px

val textInputStyles by ComponentStyle {
    base {
        Modifier.fontFamily("verdana")
    }

    hover {
        Modifier.cursor(Cursor.Text)
    }
}

val headingStyle by ComponentStyle {
    base {
        Modifier.fontFamily("verdana").fontSize(20.px)
            .color(Color.aquamarine)
            .transition(CSSTransition(property = "color", duration = 500.ms))
    }

    hover {
        Modifier.color(Color.cornflowerblue)
            .cursor(Cursor.Default)
    }
}

val buttonStyles by ComponentStyle {
    base {
        Modifier.backgroundColor(Color.aquamarine)
            .transition(CSSTransition(property = "background-color", duration = 500.ms))
            // .transition(CSSTransition(property = "color", duration = 500.ms))
            .border(
                width = 2.px,
                style = LineStyle.Solid,
                color = Color.darkblue,
            ).borderRadius(10.px)
            .color(Color.darkblue)
            .boxShadow(blurRadius = 10.px, spreadRadius = 0.2.px, color = Colors.Teal)
            .fontFamily("verdana")
    }

    hover {
        Modifier.background(Color.cornflowerblue)
            .color(Color.aquamarine)
            .cursor(Cursor.Pointer)
            .border(
                width = 2.px,
                style = LineStyle.Solid,
                color = Color.aquamarine,
            ).borderRadius(10.px)
    }

    disabled {
        Modifier.border(
            width = 2.px,
            style = LineStyle.Solid,
            color = Colors.Red,
        )
            .color(Color.red)
            .cursor(Cursor.Default)
    }
}
