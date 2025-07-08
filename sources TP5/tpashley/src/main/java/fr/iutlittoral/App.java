package fr.iutlittoral;

import fr.iutlittoral.components.Spawner;
import fr.iutlittoral.components.Target;                                                                                
import fr.iutlittoral.components.spawntypes.MovingBoxSpawnType;
import fr.iutlittoral.components.spawntypes.SimpleBoxSpawnType;
// import fr.iutlittoral.components.spawntypes.SlimeBoxSpawnType;
import fr.iutlittoral.events.TargetDestroyed;
import fr.iutlittoral.systems.*;
import fr.iutlittoral.systems.spawners.MovingboxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SimpleBoxSpawnerSystem;
import fr.iutlittoral.systems.spawners.SlimeBoxSpawnerSystem;
import fr.iutlittoral.utils.*;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.signals.Signal;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        /* Standard JavaFX stage creation */
        var canvas = new Canvas(1600, 900);
        canvas.getGraphicsContext2D().fillRect(0, 0, 1600, 900);
        var scene = new Scene(new StackPane(canvas), 1600, 900);
        stage.setScene(scene);
        stage.show();

        /* Ashley engine initialization */
        Engine world = new Engine();

        /* Helper objects initialization */
        Font font = new Font("Vera.ttf", 25);
        // Keyboard keyboard = new Keyboard(scene);
        Mouse mouse = new Mouse(canvas);
        EntityCreator creator = new EntityCreator(world);
        
        /* Adds a target spawner */
        creator.create(
            new Spawner(1, 0, 0, 1550, 850),
            new SimpleBoxSpawnType()
        );

        /* Adds a moving box spawner */
        creator.create(
            new Spawner(1, 0, 0, 1550, 850),
            new MovingBoxSpawnType()
        );

        // creator.create(
        //     new Spawner(1, 0, 0, 1550, 850),
        //     new SlimeBoxSpawnType()
        // );

        /* System registration */
        world.addSystem(new SimpleBoxSpawnerSystem(Color.GOLDENROD));
        world.addSystem(new MovingboxSpawnerSystem(Color.DARKRED));
        world.addSystem(new SlimeBoxSpawnerSystem(Color.LIGHTBLUE));
        BulletCollisionSystem bulletCollisionSystem = new BulletCollisionSystem();
        world.addSystem(bulletCollisionSystem);
        world.addSystem(new LimitedLifespanSystem());
        world.addSystem(new VelocitySystem());

        /* Score */
        Score score = new Score();
        Signal<TargetDestroyed> targetDestroyedSignal = bulletCollisionSystem.getTargetDestroyedSignal();
        targetDestroyedSignal.add(score);

        /* Explosion */
        ExplosionListener explosionListener = new ExplosionListener(Color.ORANGE, world);
        targetDestroyedSignal.add(explosionListener);

        // /* Slime */
        // SlimeListener slimeListener = new SlimeListener(world);
        // targetDestroyedSignal.add(slimeListener);
        
        AlphaDecaySystem alphaSystem = new AlphaDecaySystem();
        world.addEntityListener(Family.all(Target.class).get(), alphaSystem);
        world.addSystem(alphaSystem);
        world.addSystem(new BoxShapeRenderer(canvas));
        world.addSystem(new CircleShapeRenderer(canvas));

        GameLoopTimer timer = new GameLoopTimer() {
            @Override
            public void tick(float secondsSinceLastFrame) {
                if (mouse.isJustPressed(MouseButton.PRIMARY)) {
                    creator.createBullet(mouse.getX(), mouse.getY());
                    mouse.resetJustPressed();
                }
                GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.save();
                gc.setFill(Color.BLACK);
                gc.fillRect(0, 0, 1600, 900);
                gc.setFill(Color.WHITE);
                gc.setFont(font);
                gc.fillText("Score " + score.getScore() /*+ bulletCollisionSystem.getScore()*/, 10, 35);
                gc.restore();
                world.update(secondsSinceLastFrame);
            }
        };

        timer.start();
    }

    public static void main(String[] args) {
        launch();
    }
}   