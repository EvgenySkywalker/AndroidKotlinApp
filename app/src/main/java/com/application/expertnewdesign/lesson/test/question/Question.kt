package com.application.expertnewdesign.lesson.test.question
import android.app.Activity
import android.content.Context
import android.widget.ScrollView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.lang.Exception
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl

const val DEFAULT_TEXT_SIZE = 16f

fun test(act : Activity): ScrollView{

    //-----------------------------------
    //Генерация вопросов и запись в файл

    val file = File(act.filesDir, "questions.xml")
    file.delete()
    file.createNewFile()
    file.appendText("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<QuestionPack>\n" +
            "  <SingleAnswerQuestion questionText=\"Какой класс может сбросить всю руку и усмирить одного монстра в бою?\">\n" +
            "    <wrongAnswer>Воин</wrongAnswer>\n" +
            "    <correctAnswer>Волшебник</correctAnswer>\n" +
            "    <wrongAnswer>Клирик</wrongAnswer>\n" +
            "    <wrongAnswer>Вор</wrongAnswer>\n" +
            "  </SingleAnswerQuestion>\n" +
            "  <MultipleAnswerQuestion questionText=\"Кто из этих перечисленных классов не может воровать\">\n" +
            "    <correctAnswer>Воин</correctAnswer>\n" +
            "    <correctAnswer>Волшебник</correctAnswer>\n" +
            "    <correctAnswer>Клирик</correctAnswer>\n" +
            "    <wrongAnswer>Вор</wrongAnswer>\n" +
            "  </MultipleAnswerQuestion>\n" +
            "  <MultipleAnswerQuestion questionText=\"Какая карточка позволяет использовать предмет против правил?\">\n" +
            "    <correctAnswer>Наемничек</correctAnswer>\n" +
            "    <wrongAnswer>Рапира так-Нечестности</wrongAnswer>\n" +
            "    <wrongAnswer>Анигиляция</wrongAnswer>\n" +
            "    <correctAnswer>Чит</correctAnswer>\n" +
            "  </MultipleAnswerQuestion>\n" +
            "  <SingleAnswerQuestion questionText=\"Какой предмет ненавидит вор?\">\n" +
            "    <wrongAnswer>Сандали Омега-пендаля</wrongAnswer>\n" +
            "    <correctAnswer>Шлем заднего вида</correctAnswer>\n" +
            "    <wrongAnswer>Такого предмета нет</wrongAnswer>\n" +
            "    <wrongAnswer>Шапочка из фольги</wrongAnswer>\n" +
            "  </SingleAnswerQuestion>\n" +
            "  <DragQuestion questionText=\"Соотнесите класс и его способность\">\n" +
            "    <pair>\n" +
            "      <left>Вор</left>\n" +
            "      <right>Может воровать</right>\n" +
            "    </pair>\n" +
            "    <pair>\n" +
            "      <left>Волшебник</left>\n" +
            "      <right>Может сбросить карту и получить +3 в бою против андеда</right>\n" +
            "    </pair>\n" +
            "    <pair>\n" +
            "      <left>Воин</left>\n" +
            "      <right>Побеждает при равенстве боевой силы</right>\n" +
            "    </pair>\n" +
            "    <pair>\n" +
            "      <left>Клирик</left>\n" +
            "      <right>Может сбросить карту и получить +1 к смывке</right>\n" +
            "    </pair>\n" +
            "  </DragQuestion>\n" +
            "</QuestionPack>")

    /*val testPack = QuestionPack(act, listOf(
        SingleAnswerQuestion(act, SingleAnswerQuestionBase("SAQ", "c", listOf("w1", "w2"))),
        MultipleAnswerQuestion(act, MultipleAnswerQuestionBase("MAQ", listOf("c1", "c2"), listOf("w1", "w2"))),
        DragQuestion(act, DragQuestionBase("DQ", listOf("l1", "l2"), listOf("r1", "r2"), listOf("w1", "w2"))),
                SingleAnswerQuestion(act, SingleAnswerQuestionBase("SAQ2", "c", listOf("w1", "w2", "w3")))
    ))
    testPack.toXML(file, 0)*/

    //-----------------------------------
    //Парс вопросов из файла

    val parser = XML.QuestionParser(File(act.filesDir, "questions.xml"))
    val pack = parser.getQuestionPack(act)

    //-----------------------------------

    val scrollView = ScrollView(act)

    scrollView.addView(pack)
    return scrollView

    //act.setContentView(scrollView)
}

//Объекты парсинга
object XML{
    fun getSpacing(spacing: Int) : String{
        var tabs = ""
        for(i in (0 until spacing))
            tabs += "\t"
        return tabs
    }

    class Field(val name : String){
        val openTag = "<$name>"
        val closeTag = "</$name>"
    }

    object Fields{
        val correctAnswer = Field("correctAnswer")
        val wrongAnswer = Field("wrongAnswer")
        val left = Field("left")
        val right = Field("right")
        val pair = Field("pair")
        val SingleAnswerQuestion = Field("SingleAnswerQuestion")
        val MultipleAnswerQuestion = Field("MultipleAnswerQuestion")
        val DragQuestion = Field("DragQuestion")
        val questionText = Field("questionText")
        val QuestionPack = Field("QuestionPack")
    }


    class ParseException(parsedObject: String, parser : XmlPullParser)
        : Exception("Couldn't parse $parsedObject. Event: ${parser.eventType} Name: ${parser.name} Text: ${parser.text}")

    class QuestionParser(file : File) {

        private val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()

        init {
            parser.setInput(file.bufferedReader())
            parser.next()
        }

        fun getQuestionPack(context: Context) : QuestionPack {

            val questions = emptyList<Question>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.name == Fields.QuestionPack.name)){
                throw ParseException(Fields.QuestionPack.name, parser)
            } else {
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == Fields.QuestionPack.name)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        questions.add(
                            when (parser.name) {
                                Fields.SingleAnswerQuestion.name -> getSAQ(context)
                                Fields.MultipleAnswerQuestion.name -> getMAQ(context)
                                Fields.DragQuestion.name -> getDQ(context)
                                else -> throw ParseException(Fields.QuestionPack.name, parser)
                            })
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.QuestionPack.name, parser)
                }
                parser.next()
            }
            parser.next()

            return QuestionPack(context, questions)
        }

        private fun getSAQ(context: Context) : SingleAnswerQuestion {

            var correctAnswer = ""
            val questionText : String
            val otherOptions : MutableList<String> = emptyList<String>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.name == Fields.SingleAnswerQuestion.name)){
                throw ParseException(Fields.SingleAnswerQuestion.name, parser)
            } else {
                questionText = parser.getAttributeValue(null, Fields.questionText.name)
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == Fields.SingleAnswerQuestion.name)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            Fields.correctAnswer.name -> correctAnswer = parseCorrectAnswer()
                            Fields.wrongAnswer.name -> otherOptions.add(parseWrongAnswer())
                            else -> throw ParseException(Fields.SingleAnswerQuestion.name, parser)
                        }
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.SingleAnswerQuestion.name, parser)
                }
                parser.next()
            }
            parser.next()

            return SingleAnswerQuestion(context, SingleAnswerQuestionBase(questionText, correctAnswer, otherOptions))
        }

        private fun parseGenericString(string : String) : String{

            val result: String

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.name == string)) {
                throw ParseException(string, parser)
            }else{
                parser.next()

                if (parser.eventType != XmlPullParser.TEXT) {
                    throw ParseException(string, parser)
                } else {
                    result = parser.text
                    parser.next()
                }

                if(!(parser.eventType == XmlPullParser.END_TAG && parser.name == string)) {
                    throw ParseException(string, parser)
                }else{
                    parser.next()
                }
            }

            return result
        }

        private fun getMAQ(context: Context) : MultipleAnswerQuestion {

            val questionText : String
            val correctAnswers : MutableList<String> = emptyList<String>().toMutableList()
            val otherOptions : MutableList<String> = emptyList<String>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.name == Fields.MultipleAnswerQuestion.name)){
                throw ParseException(Fields.MultipleAnswerQuestion.name, parser)
            } else {
                questionText = parser.getAttributeValue(null, Fields.questionText.name)
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == Fields.MultipleAnswerQuestion.name)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            Fields.correctAnswer.name -> correctAnswers.add(parseCorrectAnswer())
                            Fields.wrongAnswer.name -> otherOptions.add(parseWrongAnswer())
                            else -> throw ParseException(Fields.MultipleAnswerQuestion.name, parser)
                        }
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.MultipleAnswerQuestion.name, parser)
                }
                parser.next()
            }
            parser.next()

            return MultipleAnswerQuestion(context, MultipleAnswerQuestionBase(questionText, correctAnswers, otherOptions))
        }

        private fun getDQ(context: Context) : DragQuestion {

            val questionText : String
            val leftColumn : MutableList<String> = emptyList<String>().toMutableList()
            val rightColumn : MutableList<String> = emptyList<String>().toMutableList()
            val otherOptions : MutableList<String> = emptyList<String>().toMutableList()

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.name == Fields.DragQuestion.name)){
                throw ParseException(Fields.DragQuestion.name, parser)
            } else {
                questionText = parser.getAttributeValue(null, Fields.questionText.name)
                parser.next()
            }

            while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == Fields.DragQuestion.name)) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            Fields.pair.name -> {
                                val pair = parsePair()
                                leftColumn.add(pair.first)
                                rightColumn.add(pair.second)
                            }
                            Fields.wrongAnswer.name -> otherOptions.add(parseWrongAnswer())
                            else -> throw ParseException(Fields.DragQuestion.name, parser)
                        }
                    }
                    XmlPullParser.TEXT -> {}
                    else -> throw ParseException(Fields.DragQuestion.name, parser)
                }
                parser.next()
            }
            parser.next()

            return DragQuestion(context, DragQuestionBase(questionText, leftColumn, rightColumn, otherOptions))
        }

        private fun parseCorrectAnswer() : String{
            return parseGenericString(Fields.correctAnswer.name)
        }

        private fun parseWrongAnswer() : String{
            return parseGenericString(Fields.wrongAnswer.name)
        }

        private fun parseLeft() : String {
            return parseGenericString(Fields.left.name)
        }

        private fun parseRight() : String {
            return parseGenericString(Fields.right.name)
        }

        private fun parsePair() : Pair<String, String>{

            var left = ""
            var right = ""

            if(!(parser.eventType == XmlPullParser.START_TAG && parser.name == Fields.pair.name)) {
                throw ParseException(Fields.pair.name, parser)
            }else{
                parser.next()
                while (!(parser.eventType == XmlPullParser.END_TAG && parser.name == Fields.pair.name)) {
                    when (parser.eventType) {
                        XmlPullParser.START_TAG -> {
                            when (parser.name) {
                                Fields.left.name -> left = parseLeft()
                                Fields.right.name -> right = parseRight()
                                else -> throw ParseException(Fields.pair.name, parser)
                            }
                        }
                        XmlPullParser.TEXT -> {}
                        else -> throw ParseException(Fields.pair.name, parser)
                    }
                    parser.next()
                }
                parser.next()
            }

            return Pair(left, right)
        }
    }


}

//Сущность любого вопроса
abstract class QuestionBase(val questionText: String) {
    abstract fun toXML(file : File, spacing : Int)
}

//Вопрос с единственным правильным вариантом ответа
@Serializable
class SingleAnswerQuestionBase(questionText: String = "", val correctAnswer : String = "",
                               val incorrectAnswers : List<String> = emptyList()) : QuestionBase(questionText){

    override fun toXML(file : File, spacing : Int) {
        //Формируем отступ
        val tabs = XML.getSpacing(spacing)

        //Открывающий тэг
        file.appendText("$tabs<${XML.Fields.SingleAnswerQuestion.name} ${XML.Fields.questionText.name}=\"$questionText\">\n")

        //Правильный ответ
        file.appendText("$tabs\t${XML.Fields.correctAnswer.openTag}$correctAnswer${XML.Fields.correctAnswer.closeTag}\n")

        //Неправильные ответы
        for(option in incorrectAnswers)
            file.appendText("$tabs\t${XML.Fields.wrongAnswer.openTag}$option${XML.Fields.wrongAnswer.closeTag}\n")

        //Закрывающий тэг
        file.appendText("$tabs${XML.Fields.SingleAnswerQuestion.closeTag}\n")
    }


    @Serializer(forClass = SingleAnswerQuestionBase::class) companion object : KSerializer<SingleAnswerQuestionBase> {
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("SingleAnswerQuestion") {
            init {
                addElement("questionText")
                addElement("correctAnswer")
                addElement("incorrectAnswers")
            }
        }

        override fun serialize(encoder: Encoder, obj: SingleAnswerQuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionText)
            compositeOutput.encodeStringElement(descriptor, 1, obj.correctAnswer)
            compositeOutput.encodeSerializableElement( descriptor, 2, String.serializer().list, obj.incorrectAnswers)
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): SingleAnswerQuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionText: String? = null
            var correctAnswer: String? = null
            var incorrectAnswers: List<String>? = null
            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionText = dec.decodeStringElement(descriptor, i)
                    1 -> correctAnswer = dec.decodeStringElement(descriptor, i)
                    2 -> incorrectAnswers = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return SingleAnswerQuestionBase(
                questionText ?: throw MissingFieldException("questionText"),
                correctAnswer ?: throw MissingFieldException("correctAnswer"),
                incorrectAnswers ?: throw MissingFieldException("incorrectAnswers")
            )
        }
    }
}

//Вопрос с множеством правильных вариантов ответа
@Serializable
class MultipleAnswerQuestionBase(questionText: String = "", var correctAnswers : List<String> = emptyList(),
                                 var incorrectAnswers : List<String> = emptyList()) : QuestionBase(questionText){

    override fun toXML(file : File, spacing : Int) {
        //Формируем отступ
        val tabs = XML.getSpacing(spacing)

        //Открывающий тэг
        file.appendText("$tabs<${XML.Fields.MultipleAnswerQuestion.name} ${XML.Fields.questionText.name}=\"$questionText\">\n")

        //Правильные ответы
        for(correctAnswer in correctAnswers)
            file.appendText("$tabs\t${XML.Fields.correctAnswer.openTag}$correctAnswer${XML.Fields.correctAnswer.closeTag}\n")

        //Неправильные ответы
        for(option in incorrectAnswers)
            file.appendText("$tabs\t${XML.Fields.wrongAnswer.openTag}$option${XML.Fields.wrongAnswer.closeTag}\n")

        //Закрывающий тэг
        file.appendText("$tabs${XML.Fields.MultipleAnswerQuestion.closeTag}\n")
    }


    @Serializer(forClass = MultipleAnswerQuestionBase::class) companion object : KSerializer<MultipleAnswerQuestionBase>{
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("MultipleAnswerQuestion") {
            init {
                addElement("questionText")
                addElement("correctAnswers")
                addElement("incorrectAnswers")
            }
        }

        override fun serialize(encoder: Encoder, obj: MultipleAnswerQuestionBase) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionText)
            compositeOutput.encodeSerializableElement(descriptor, 1, String.serializer().list, obj.correctAnswers)
            compositeOutput.encodeSerializableElement(descriptor, 1, String.serializer().list, obj.incorrectAnswers)
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): MultipleAnswerQuestionBase {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionText : String? = null
            var correctAnswers : List<String>? = null
            var incorrectAnswers : List<String>? = null

            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionText = dec.decodeStringElement(descriptor, i)
                    1 -> correctAnswers = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    2 -> incorrectAnswers = dec.decodeSerializableElement(descriptor, i, String.serializer().list)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)

            return MultipleAnswerQuestionBase(
                questionText ?: throw MissingFieldException("questionText"),
                correctAnswers ?: throw MissingFieldException("correctAnswers"),
                incorrectAnswers ?: throw MissingFieldException("incorrectAnswers")

            )
        }
    }
}

@Serializable
class QuestionMetadata(val questionType : String = "", val questionBase: QuestionBase){
    @Serializer(forClass = QuestionMetadata::class) companion object : KSerializer<QuestionMetadata>{
        override val descriptor: SerialDescriptor = object : SerialClassDescImpl("BinaryPayload") {
            init {
                addElement("questionType")
                addElement("questionBase")
            }
        }

        override fun serialize(encoder: Encoder, obj: QuestionMetadata) {
            val compositeOutput = encoder.beginStructure(descriptor)
            compositeOutput.encodeStringElement(descriptor, 0, obj.questionType)
            when(obj.questionType){
                "SingleAnswerQuestion" ->
                    compositeOutput.encodeSerializableElement(descriptor, 1,
                        SingleAnswerQuestionBase.serializer(), obj.questionBase as SingleAnswerQuestionBase)
                "MultipleAnswerQuestion" ->
                    compositeOutput.encodeSerializableElement(descriptor, 1,
                        MultipleAnswerQuestionBase.serializer(), obj.questionBase as MultipleAnswerQuestionBase)
                else -> throw SerializationException("Unknown questionType")
            }
            compositeOutput.endStructure(descriptor)
        }

        override fun deserialize(decoder: Decoder): QuestionMetadata {
            val dec: CompositeDecoder = decoder.beginStructure(descriptor)
            var questionType : String? = null
            var questionBase : QuestionBase? = null

            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> questionType = dec.decodeStringElement(descriptor, i)
                    1 -> questionBase = dec.decodeSerializableElement(descriptor, i,
                        when(questionType){
                            "SingleAnswerQuestion" -> SingleAnswerQuestionBase.serializer()
                            "MultipleAnswerQuestion" -> MultipleAnswerQuestionBase.serializer()
                            null -> throw SerializationException("questionType is yet unknown")
                            else -> throw SerializationException("Unknown questionType")
                        }
                    )
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)

            return QuestionMetadata(
                questionType ?: throw MissingFieldException("questionType"),
                questionBase ?: throw MissingFieldException("questionBase")
            )
        }
    }
}

//Вопрос на сопоставление
class DragQuestionBase(questionText: String = "", var leftColumn: List<String> = emptyList(),
                       var rightColumn: List<String> = emptyList(), var otherOptions : List<String> = emptyList())
    : QuestionBase(questionText){

    override fun toXML(file : File, spacing : Int) {
        //Формируем отступ
        val tabs = XML.getSpacing(spacing)

        //Открывающий тэг
        file.appendText("$tabs<${XML.Fields.DragQuestion.name} ${XML.Fields.questionText.name}=\"$questionText\">\n")

        //Правильные ответы
        for(i in (0 until leftColumn.size)){
            file.appendText("$tabs\t${XML.Fields.pair.openTag}\n")
            file.appendText("$tabs\t\t${XML.Fields.left.openTag}${leftColumn[i]}${XML.Fields.left.closeTag}\n")
            file.appendText("$tabs\t\t${XML.Fields.right.openTag}${rightColumn[i]}${XML.Fields.right.closeTag}\n")
            file.appendText("$tabs\t${XML.Fields.pair.closeTag}\n")
        }

        //Неправильные ответы
        for(option in otherOptions)
            file.appendText("$tabs\t${XML.Fields.wrongAnswer.openTag}$option${XML.Fields.wrongAnswer.closeTag}\n")

        //Закрывающий тэг
        file.appendText("$tabs${XML.Fields.DragQuestion.closeTag}\n")
    }



}
