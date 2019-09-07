package haivo.us.scanmusicpro.model.xml

import com.tickaroo.tikxml.annotation.Attribute
import com.tickaroo.tikxml.annotation.Element
import com.tickaroo.tikxml.annotation.Xml


@Xml(name = "score-partwise")
data class ScorePartWise(

        @Attribute
        val version: String,

        @Element
        val identification: Identification,


        @Element
        val partList: PartList,


        @Element
        val parts: List<Part>

)