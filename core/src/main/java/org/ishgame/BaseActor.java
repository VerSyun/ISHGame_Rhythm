package org.ishgame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Intersector.MinimumTranslationVector;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;

/**
 * Расширяет функциональность класса Actor из LibGDX.
 * Добавляет поддержку текстур/анимаций,
 * движения, границ мира, и прокрутки камеры.
 * Большинство игровых объектов должны расширять этот класс; списки расширений можно получить по названию этапа и класса.
 *
 * @author Lee Stemkoski
 * @see #Actor
 */
public class BaseActor extends Group { //Класс, заимствованный у Ли Стемкоски, расширяет функциональность класса Actor из LibGDX.

    private Animation<TextureRegion> animation;
    private float elapsedTime;
    private boolean animationPaused;

    private Vector2 velocityVec;
    private Vector2 accelerationVec;
    private float acceleration;
    private float maxSpeed;
    private float deceleration;

    private Polygon boundaryPolygon;

    // сохраняет размер игрового мира для всех экземпляров класса Actor
    private static Rectangle worldBounds;

    public BaseActor(float x, float y, Stage stage) {
        // вызывает конструктор класса Actor
        super();

        // выполняет дополнительные задачи инициализации
        setPosition(x, y);
        stage.addActor(this);

        // инициализация данных анимации
        animation = null;
        elapsedTime = 0;
        animationPaused = false;

        // инициализация данных физики
        velocityVec = new Vector2(0, 0);
        accelerationVec = new Vector2(0, 0);
        acceleration = 0;
        maxSpeed = 1000;
        deceleration = 0;

        boundaryPolygon = null;
    }

    /**
     * Если объект полностью выходит за границы мира,
     * корректирует его положение на противоположную сторону мира.
     */
    public void wrapAroundWorld() {
        if (getX() + getWidth() < 0)
            setX(worldBounds.width);

        if (getX() > worldBounds.width)
            setX(-getWidth());

        if (getY() + getHeight() < 0)
            setY(worldBounds.height);

        if (getY() > worldBounds.height)
            setY(-getHeight());
    }

    /**
     * Выравнивание центра Actor по заданным координатам.
     *
     * @param x x-коордианата центра
     * @param y y-координата центра
     */
    public void centerAtPosition(float x, float y) {
        setPosition(x - getWidth() / 2, y - getHeight() / 2);
    }

    /**
     * Перемещает этот BaseActor так, чтобы его центр был выровнен
     * с центром другого BaseActor. Полезно, когда один BaseActor порождает другой.
     *
     * @param other BaseActor с которым нужно выровнять этот BaseActor
     */
    public void centerAtActor(BaseActor other) {
        centerAtPosition(other.getX() + other.getWidth() / 2, other.getY() + other.getHeight() / 2);
    }

    // ----------------------------------------------
    // Методы анимации
    // ----------------------------------------------

    /**
     * Задает анимацию при рендере Actor; также задает его размер.
     *
     * @param animation анимация, которая будет нарисована, когда рендер завершен.
     */
    public void setAnimation(Animation<TextureRegion> animation) {
        this.animation = animation;
        TextureRegion textureRegion = this.animation.getKeyFrame(0);
        float regionWidth = textureRegion.getRegionWidth();
        float regionHeight = textureRegion.getRegionHeight();
        setSize(regionWidth, regionHeight);
        setOrigin(regionWidth / 2, regionHeight / 2);

        if (boundaryPolygon == null)
            setBoundaryRectangle();
    }

    /**
     * Создает анимацию из изображений, которые сохранены в разных файлах.
     *
     * @param fileNames     список имен файлов, содержащих изображения анимации
     * @param frameDuration как долго каждый кадр должен быть отображен
     * @param loop          для зацикливания анимации
     * @return возвращает созданную анимацию (полезно для сохранения нескольких анимаций)
     */
    public Animation<TextureRegion> loadAnimationFromFiles(String[] fileNames, float frameDuration, boolean loop) {
        int fileCount = fileNames.length;
        Array<TextureRegion> textureArray = new Array<>();

        for (int n = 0; n < fileCount; n++) {
            String fileName = fileNames[n];
            Texture texture = new Texture(Gdx.files.internal(fileName));
            texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            textureArray.add(new TextureRegion(texture));
        }

        Animation<TextureRegion> animation = new Animation<>(frameDuration, textureArray);
        animation.setPlayMode(Animation.PlayMode.NORMAL);

        if (loop)
            animation.setPlayMode(Animation.PlayMode.LOOP);

        if (this.animation == null)
            setAnimation(animation);

        return animation;
    }

    /**
     * Создает анимацию из спрайт-листа: прямоугольной сетки изображений, хранящихся в одном файле..
     *
     * @param fileName      имя файла, содержащего спрайт-лист
     * @param rows          количество строк изображений в спрайт-листе
     * @param cols          количество столбцов изображений в листе спрайта
     * @param frameDuration как долго каждый кадр должен быть отображен
     * @param loop          для зацикливания анимации
     * @return возвращает созданную анимацию (полезно для сохранения нескольких анимаций)
     */
    public Animation<TextureRegion> loadAnimationFromSheet(String fileName, int rows, int cols, float frameDuration, boolean loop) {
        Texture texture = new Texture(Gdx.files.internal(fileName), true);
        texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        int frameWidth = texture.getWidth() / cols;
        int frameHeight = texture.getHeight() / rows;

        TextureRegion[][] temp = TextureRegion.split(texture, frameWidth, frameHeight);

        Array<TextureRegion> textureArray = new Array<>();

        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++)
                textureArray.add(temp[r][c]);

        Animation<TextureRegion> animation = new Animation<>(frameDuration, textureArray);
        animation.setPlayMode(Animation.PlayMode.NORMAL);

        if (loop)
            animation.setPlayMode(Animation.PlayMode.LOOP);

        if (this.animation == null)
            setAnimation(animation);

        return animation;
    }

    /**
     * Удобный метод создания покадровой анимации из одной текстуры.
     *
     * @param fileName имена файлов изображений
     * @return возвращает созданную анимацию (полезно для сохранения нескольких анимаций)
     */
    public Animation<TextureRegion> loadTexture(String fileName) {
        String[] fileNames = new String[1];
        fileNames[0] = fileName;
        return loadAnimationFromFiles(fileNames, 1, true);
    }

    /**
     * Установка состояние паузы анимации.
     *
     * @param pause true для паузы, false для продолжения анимации
     */
    public void setAnimationPaused(boolean pause) {
        animationPaused = pause;
    }

    /**
     * Проверяет, завершена ли анимация: если режим воспроизведения нормальный (не лупа)
     * и прошедшее время больше, чем время, соответствующее последнему кадру.
     *
     * @return
     */
    public boolean isAnimationFinished() {
        return animation.isAnimationFinished(elapsedTime);
    }

    /**
     * Задает прозрачность этого Actor.
     *
     * @param opacity значения от: 0 (прозрачный) to 1 (непрозрачный)
     */
    public void setOpacity(float opacity) {
        this.getColor().a = opacity;
    }

    // ----------------------------------------------
    // Physics/Motion methods
    // ----------------------------------------------

    /**
     * Задает ускорение этого Actor.
     *
     * @param acceleration Ускорение в (пикселях в секунду) в секунду.
     */
    public void setAcceleration(float acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Задает ускорение для объекта.
     * Замедление применяется только тогда, когда объект не ускоряется.
     *
     * @param deceleration Замедление в (пикселях в секунду) в секунду.
     */
    public void setDeceleration(float deceleration) {
        this.deceleration = deceleration;
    }

    /**
     * Задает максимальную скорость объекта.
     *
     * @param maxSpeed Максимальная скорость объекта в пискелях в секунду.
     */
    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    /**
     * Установка скорости движения (в пикселях в секунду) в текущем направлении.
     * Если текущая скорость равна нулю (направление не определено), направление будет установлено на 0 градусов..
     *
     * @param speed скорость движения (пиксели в секунду)
     */
    public void setSpeed(float speed) {
        // if length is zero, then assume motion angle is zero degrees
        if (velocityVec.len() == 0)
            velocityVec.set(speed, 0);
        else
            velocityVec.setLength(speed);
    }

    /**
     * Рассчитывает скорость движения (в пикселях в секунду).
     *
     * @return возвращает скорость движения (в пикселях в секунду)
     */
    public float getSpeed() {
        return velocityVec.len();
    }

    /**
     * Определяет, движется ли данный объект (если скорость больше нуля)..
     *
     * @return возвращает false если скорость 0, true во всех остальных случаях.
     */
    public boolean isMoving() {
        return (getSpeed() > 0);
    }

    /**
     * Задает угол движения (в градусах).
     * Если текущая скорость равна нулю, это не будет иметь никакого эффекта.
     *
     * @param angle угол движения (градусы)
     */
    public void setMotionAngle(float angle) {
        velocityVec.setAngleDeg(angle);
    }

    /**
     * Получает угол движения (в градусах), рассчитывается из вектора скорости.
     * <br>
     * Чтобы выровнять угол изображения актера с углом движения, используйте <code>setRotation( getMotionAngle() )</code>..
     *
     * @return возвращает угол движения (градусы)
     */
    public float getMotionAngle() {
        return velocityVec.angleDeg();
    }

    /**
     * Обновление вектора ускорения по углу и значению, сохраненному в поле ускорения.
     * Ускорение применяется методом <code>applyPhysics</code>.
     *
     * @param angle Угол (в градусах), под которым необходимо ускориться.
     * @see #acceleration
     * @see #applyPhysics
     */
    public void accelerateAtAngle(float angle) {
        accelerationVec.add(
                new Vector2(acceleration, 0).setAngleDeg(angle));
    }

    /**
     * Обновление вектора ускорения на текущий угол поворота и значение, сохраненное в поле ускорения.
     * Ускорение применяется методом <code>applyPhysics</code>.
     *
     * @see #acceleration
     * @see #applyPhysics
     */
    public void accelerateForward() {
        accelerateAtAngle(getRotation());
    }

    /**
     * Настраивает вектор скорости на основе вектора ускорения,
     * затем корректирует положение на основе вектора скорости. <br>
     * Если не ускоряется, применяется значение замедления. <br>
     * Скорость ограничена значением maxSpeed. <br>
     * Сброс вектора ускорения на (0,0) в конце метода. <br>
     *
     * @param deltaTime Время, прошедшее с предыдущего кадра (deltalime); обычно получается из метода <code>act</code>..
     * @see #acceleration
     * @see #deceleration
     * @see #maxSpeed
     */
    public void applyPhysics(float deltaTime) {
        // применяет ускорение
        velocityVec.add(accelerationVec.x * deltaTime, accelerationVec.y * deltaTime);

        float speed = getSpeed();

        // снижает скорость (замедляет), когда нет ускорения
        if (accelerationVec.len() == 0)
            speed -= deceleration * deltaTime;

        // поддерживает скорость в заданных пределах
        speed = MathUtils.clamp(speed, 0, maxSpeed);

        // обновляет скорость
        setSpeed(speed);

        // обновление позиции в соответствии со значением, хранящимся в векторе скорости
        moveBy(velocityVec.x * deltaTime, velocityVec.y * deltaTime);

        // сбрасывает ускорение
        accelerationVec.set(0, 0);
    }

    // ----------------------------------------------
    // Методы полигонов столкновения
    // ----------------------------------------------

    /**
     * Устанавливает полигон столкновения прямоугольной формы.
     * Этот метод автоматически вызывается при установке анимации,
     * при условии, что текущий граничный полигон является нулевым (null).
     *
     * @see #setAnimation
     */
    public void setBoundaryRectangle() {
        float w = getWidth();
        float h = getHeight();

        float[] vertices = {0, 0, w, 0, w, h, 0, h};
        boundaryPolygon = new Polygon(vertices);
    }

    /**
     * Заменяет прямоугольник столкновения по умолчанию (прямоугольник) на n-сторонний многоугольник. <br>
     * Вершины многоугольника лежат на эллипсе, содержащемся в ограничивающем прямоугольнике.
     * Примечание: одна вершина будет расположена в точке (0,ширина);
     * появится 4-сторонний многоугольник в ориентации ромба.
     *
     * @param numSides количество сторон прямоугольника столкновения
     */
    public void setBoundaryPolygon(int numSides) {
        float w = getWidth();
        float h = getHeight();

        float[] vertices = new float[2 * numSides];
        for (int i = 0; i < numSides; i++) {
            float angle = i * 6.28f / numSides;
            // x-coordinate
            vertices[2 * i] = w / 2 * MathUtils.cos(angle) + w / 2;
            // y-coordinate
            vertices[2 * i + 1] = h / 2 * MathUtils.sin(angle) + h / 2;
        }
        boundaryPolygon = new Polygon(vertices);

    }

    /**
     * Возвращает ограничивающий многоугольник для этого BaseActor, скорректированный текущим положением и вращением Actor'а..
     *
     * @return ограничивающий многоугольник для этого BaseActor
     */
    public Polygon getBoundaryPolygon() {
        boundaryPolygon.setPosition(getX(), getY());
        boundaryPolygon.setOrigin(getOriginX(), getOriginY());
        boundaryPolygon.setRotation(getRotation());
        boundaryPolygon.setScale(getScaleX(), getScaleY());
        return boundaryPolygon;
    }

    /**
     * Определить, перекрывает ли данный BaseActor другой BaseActor (в соответствии с полигонами столкновения).
     *
     * @param other BaseActor для проверки перекрытия
     * @return true, если полигоны столкновения этого и другого BaseActor перекрываются
     * @see #setBoundaryRectangle
     * @see #setBoundaryPolygon
     */
    public boolean overlaps(BaseActor other) {
        Polygon currentPoly = this.getBoundaryPolygon();
        Polygon otherPoly = other.getBoundaryPolygon();

        // initial test to improve performance
        if (!currentPoly.getBoundingRectangle().overlaps(otherPoly.getBoundingRectangle()))
            return false;

        return Intersector.overlapConvexPolygons(currentPoly, otherPoly);
    }

    /**
     * Реализовать "твердое" поведение:
     * если есть перекрытие, перемещает этот базовый вектор в сторону от другого базового вектора
     * вдоль минимального вектора перевода до тех пор, пока не останется перекрытия.
     *
     * @param other BaseActor для проверки перекрытия
     * @return вектор направления, по которому был переведен актер, null, если нет перекрытия
     */
    public Vector2 preventOverlap(BaseActor other) {
        Polygon currentPoly = this.getBoundaryPolygon();
        Polygon otherPoly = other.getBoundaryPolygon();

        // initial test to improve performance
        if (!currentPoly.getBoundingRectangle().overlaps(otherPoly.getBoundingRectangle()))
            return null;

        MinimumTranslationVector mtv = new MinimumTranslationVector();
        boolean polygonOverlap = Intersector.overlapConvexPolygons(currentPoly, otherPoly, mtv);

        if (!polygonOverlap)
            return null;

        this.moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth);
        return mtv.normal;
    }

    /**
     * Определить, находится ли данный BaseActor рядом с другим BaseActor (согласно полигонам столкновения).
     *
     * @param distance количество (пикселей), на которое следует увеличить ширину и высоту полигона столкновения
     * @param other    BaseActor для проверки наличия поблизости
     * @return true, если полигоны столкновения этого (увеличенного) и другого BaseActor перекрывают друг друга
     * @see #setBoundaryRectangle
     * @see #setBoundaryPolygon
     */
    public boolean isWithinDistance(float distance, BaseActor other) {
        Polygon currentPoly = this.getBoundaryPolygon();
        float scaleX = (this.getWidth() + 2 * distance) / this.getWidth();
        float scaleY = (this.getHeight() + 2 * distance) / this.getHeight();
        currentPoly.setScale(scaleX, scaleY);

        Polygon otherPoly = other.getBoundaryPolygon();

        // initial test to improve performance
        if (!currentPoly.getBoundingRectangle().overlaps(otherPoly.getBoundingRectangle()))
            return false;

        return Intersector.overlapConvexPolygons(currentPoly, otherPoly);
    }

    /**
     * Устанавливает размеры мира для использования методами boundToWorld() и scrollTo().
     *
     * @param width  ширина мира
     * @param height высота мира
     */
    public static void setWorldBounds(float width, float height) {
        worldBounds = new Rectangle(0, 0, width, height);
    }

    /**
     * Устанавливает размеры мира для использования методами boundToWorld() и scrollTo().
     *
     * @param BaseActor, размер которого определяет границы мира (обычно это фоновое изображение)
     */
    public static void setWorldBounds(BaseActor referenceActor) {
        setWorldBounds(referenceActor.getWidth(), referenceActor.getHeight());
    }

    /**
     * Получает размеры мира
     *
     * @return возвращает Rectangle, ширина/высота которого представляют границы мира
     */
    public static Rectangle getWorldBounds() {
        return worldBounds;
    }

    /**
     * Если край объекта перемещается за границы мира,
     * регулирует его положение, чтобы он полностью находился на экране.
     */
    public void boundToWorld() {
        if (getX() < 0)
            setX(0);
        if (getX() + getWidth() > worldBounds.width)
            setX(worldBounds.width - getWidth());
        if (getY() < 0)
            setY(0);
        if (getY() + getHeight() > worldBounds.height)
            setY(worldBounds.height - getHeight());
    }

    /**
     * Центрируйте камеру на этом объекте, сохраняя диапазон обзора камеры
     * (определяется размером экрана) полностью в пределах мировых границ.
     */
    public void alignCamera() {
        Camera cam = this.getStage().getCamera();
        Viewport v = this.getStage().getViewport();

        // center camera on actor
        cam.position.set(this.getX() + this.getOriginX(), this.getY() + this.getOriginY(), 0);

        // bound camera to layout
        cam.position.x = MathUtils.clamp(cam.position.x, cam.viewportWidth / 2, worldBounds.width - cam.viewportWidth / 2);
        cam.position.y = MathUtils.clamp(cam.position.y, cam.viewportHeight / 2, worldBounds.height - cam.viewportHeight / 2);
        cam.update();
    }

    // ----------------------------------------------
    // Методы списка экземпляров
    // ----------------------------------------------

    /**
     * Извлекает список всех экземпляров объекта с заданного этапа с заданным именем класса
     * или чей класс расширяет класс с заданным именем.
     * Если экземпляров не существует, возвращается пустой список.
     * Полезно при кодировании взаимодействия между различными типами игровых объектов в методе update.
     *
     * @param stage     Сцена (Stage), содержащая экземпляры BaseActor
     * @param className имя класса, который расширяет класс BaseActor
     * @return возвращает список экземпляров объекта на этапе, которые расширяются с помощью заданного имени класса
     */
    public static ArrayList<BaseActor> getList(Stage stage, String className) {

        ArrayList<BaseActor> list = new ArrayList<>();

        Class theClass = null;

        try {
            theClass = Class.forName(className);
        } catch (Exception error) {
            error.printStackTrace();
        }

        for (Actor a : stage.getActors()) {
            if (theClass.isInstance(a))
                list.add((BaseActor) a);
        }

        return list;
    }

    /**
     * Возвращает количество экземпляров заданного класса (который расширяет BaseActor).
     *
     * @param className имя класса, который расширяет класс BaseActor
     * @return количество экземпляров класса
     */
    public static int count(Stage stage, String className) {
        return getList(stage, className).size();
    }

    // ----------------------------------------------
    // методы класса Actor: act и draw
    // ----------------------------------------------

    /**
     * Обрабатывает все действия (Actions) и связанный с ними код для этого объекта;
     * автоматически вызывается методом act в классе Stage.
     *
     * @param deltaTime прошедшее время (секунда) с момента последнего кадра (предоставляется методом Stage act)
     */
    public void act(float deltaTime) {
        super.act(deltaTime);

        if (!animationPaused)
            elapsedTime += deltaTime;
    }

    /**
     * Рисует текущий кадр анимации; автоматически вызывается методом draw в классе Stage. <br>
     * Если был задан цвет, изображение будет тонировано этим цветом. <br>
     * Если анимация не задана или объект невидим, ничего не будет нарисовано..
     *
     * @param batch       (поставляется по методу Stage draw)
     * @param parentAlpha (поставляется по методу Stage draw)
     * @see #setColor
     * @see #setVisible
     */
    public void draw(Batch batch, float parentAlpha) {

        // применяет эффект оттенка цвета
        Color c = getColor();
        batch.setColor(c.r, c.g, c.b, c.a);

        if (animation != null && isVisible())
            batch.draw(animation.getKeyFrame(elapsedTime),
                    getX(), getY(), getOriginX(), getOriginY(),
                    getWidth(), getHeight(), getScaleX(), getScaleY(), getRotation());

        super.draw(batch, parentAlpha);
    }
}
