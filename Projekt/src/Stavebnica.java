import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Adagon
 * @version 1.1
 * Jednoducha hra pre deti
 */
public class Stavebnica extends Application {

    private  Group group = new Group();
    BorderPane root= new BorderPane();

    private static final int WIDTH = 1700;
    private static final int HEIGHT = 1000;
    private int ROWS = 5;

    private double klikX, klikY;
    private double klikAngleX = 0;
    private double klikAngleY = 0;
    private final DoubleProperty angleX = new SimpleDoubleProperty(40);
    private final DoubleProperty angleY = new SimpleDoubleProperty(90);

    private List<PlaygroundBox>[][] poleDoska = new ArrayList[11][11];
    private PlaygroundButton[][] poleGrid = new PlaygroundButton[11][11];

    private PlaygroundPane pp;
    private PhongMaterial boxMaterial = new PhongMaterial();
    private Label r;
    private Double diffikulty = 0.25;

    /**
     * inicializuje celu aplikaciu
     */
    @Override
    public void start(Stage primaryStage) {

        Camera camera = new PerspectiveCamera();
        camera.setFarClip(500);
        SubScene subscene = new SubScene(group, WIDTH, HEIGHT,true,SceneAntialiasing.BALANCED);
        subscene.setFill(Color.SILVER);
        subscene.setCamera(camera);
        root.setCenter(subscene);
        root.setLeft(initUI());
        Scene scene = new Scene(root, WIDTH, HEIGHT);

        boxMaterial.setDiffuseMap(new Image("file:Red_texture.jpg"));

        group.translateXProperty().set(WIDTH / 2 - 20);
        group.translateYProperty().set(HEIGHT / 2);
        group.translateZProperty().set(-1400);

        initMouseControl(group, scene);
        initPlain(group);

        primaryStage.setTitle("Stavebnica");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * inicializuje UI
     * @return vertikalny panel
     */
    private VBox initUI() {
        Button skontroluj = new Button("Skontroluj");
        skontroluj.setOnMousePressed(event -> kontrola());
        Button rS = new Button("Nahodna stavba");
        rS.setOnMouseClicked(ev-> randomBuild());
        Button rP = new Button("Nahodny podoris");
        rP.setOnMouseClicked(ev-> randomGrid());
        Label l = new Label("Vitaj v Stavebnici!\n Kliknutim laveho tlacitka mysi pridavas bloky, pravym odoberas. \n Tvoja stavebnica vsak musi mat aj spravny podoris!\n " +
                "Vygeneruj si nahodnu stavbu alebo nahodny podoris. \n Nastav si obtiaznost:" );
        r = new Label();
        r.setStyle("-fx-font-size: 20;");
        l.setTextAlignment(TextAlignment.CENTER);
        pp = new PlaygroundPane();
        pp.setAlignment(Pos.CENTER);
        Slider dif = new Slider(0.15,0.31, 0.25);
        dif.setOnMouseDragged(ev -> {diffikulty = dif.getValue();
            ROWS = (int)((dif.getValue()-0.10)/ 0.20 * 4) * 2 + 3;
            clear();
            initPlain(group);
        });
        dif.setOnMouseClicked(ev -> {diffikulty = dif.getValue();
            ROWS = (int)((dif.getValue()-0.10)/ 0.20 * 4) * 2 + 3;
            clear();
            initPlain(group);
        });
        VBox Panel = new VBox(pp, skontroluj, rS, rP, l, dif, r);
        Panel.setAlignment(Pos.CENTER);
        Panel.setSpacing(40);
        return Panel;
    }

    /**
     * vygeneruje nakodny podoris
     */
    private void randomGrid() {
        clear();
        initPlain(group);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < ROWS; j++) {
                int o = 0;
                while(Math.random()<diffikulty){ o++;}
                poleGrid[i][j].setValue(o);
            }
        }
    }

    /**
     * vycisti podoris aj stavebnicu
     */
    private void clear() {
        r.setText(" ");
        r.setStyle("-fx-font-size: 20;");
        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if(poleGrid[i][j]!=null) pp.getChildren().remove(poleGrid[i][j]);
                while (poleDoska[i][j].size() != 0) {
                    group.getChildren().remove(poleDoska[i][j].get(0));
                    poleDoska[i][j].remove(0);
                }
            }
        }
        pp.update();
    }

    /**
     * postavi random stavebnicu
     */
    private void randomBuild() {
        clear();
        initPlain(group);
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < ROWS; j++) {
                while(Math.random()<diffikulty){
                    PlaygroundBox prdchodca = poleDoska[i][j].get(poleDoska[i][j].size()-1);
                    PlaygroundBox tmp = new PlaygroundBox(prdchodca.value+1,i,j,prdchodca.getTranslateX(),prdchodca.getTranslateY()-20,prdchodca.getTranslateZ());
                    poleDoska[i][j].add(tmp);
                    group.getChildren().add(tmp);
                }
            }
        }
    }


    /**
     * skontroluje podoris so stavebnicou
     */
    private void kontrola() {
        boolean first = true, second = true, third = true, fourth = true;

        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                if(poleDoska[i][j].size()!=0) {
                    first &= poleDoska[i][j].size() - 1 == poleGrid[i][j].value;
                    second &= poleDoska[i][j].size() - 1 == poleGrid[ROWS-1 - j][i].value;
                    third &= poleDoska[i][j].size() - 1 == poleGrid[j][ROWS-1 - i].value;
                    fourth &= poleDoska[i][j].size() - 1 == poleGrid[ROWS-1 - i][ROWS-1 - j].value;
                }
            }
        }
        if(first||second||third||fourth){
            r.setText(" Gratulujem spravne! ");
            r.setStyle("-fx-text-fill: green; -fx-font-size: 20; -fx-background-color: lightgreen; -fx-border-width: 2; -fx-border-color: black");
            if(first){ animate(90,40); }
            else if(second){ animate(0,40); }
            else if(third){ animate(180,40); }
            else if(fourth){ animate(270,40); }

        }else {
            r.setText(" Su tam este nejake chyby! ");
            r.setStyle("-fx-text-fill: darkred; -fx-font-size: 20; -fx-background-color: indianred; -fx-border-width: 2; -fx-border-color: black");
        }
    }

    /**
     * animuje natacanie stavebnice
     */
    private void animate(double Y, double X) {
        Timeline tl = new Timeline(new KeyFrame(new Duration(5), e -> {
            if (Math.abs(angleY.get())!=Y) angleY.set(angleY.get()+ (angleY.get()>Y? -1: +1));
            if (Math.abs(Math.abs(angleX.get())-X)>2) angleX.set(angleX.get()+ (angleX.get()>X? -1: +1));
            if (Math.abs(group.translateZProperty().get()/10)*10!=1400) group.translateZProperty().set(group.translateZProperty().get()+ (group.translateZProperty().get()>-1400? -10: +10));
        }));
        tl.setCycleCount((int)(angleY.get()>Y? angleY.get()-Y: Y - angleY.get() ));
        tl.play();
    }

    /**
     * inicializuje hracou dosku
     */
    private void initPlain(Group group) {
        PhongMaterial PlainMaterial = new PhongMaterial();
        PlainMaterial.setDiffuseMap(new Image("file:White_texture.jpg"));
        for (int i = -5; i < 6; i++) {
            for (int j = -5; j < 6; j++) {
                poleDoska[i+5][j+5]= new ArrayList<>();
                if (i>=-ROWS/2 && j>= -ROWS/2 && i<=ROWS/2 && j<= ROWS/2) {
                    PlaygroundBox box = new PlaygroundBox(0, i + ROWS/2, j + ROWS/2, i * 20, 5, j * 20);
                    box.setMaterial(PlainMaterial);
                    poleDoska[i + ROWS/2][j + ROWS/2].add(box);
                    group.getChildren().add(box);
                }
            }
        }

    }

    /**
     * inicializuje ovladanie mysou
     */
    private void initMouseControl(Group group, Scene scene) {
        Rotate xRotate;
        Rotate yRotate;
        group.getTransforms().addAll(
                xRotate = new Rotate(0, Rotate.X_AXIS),
                yRotate = new Rotate(0, Rotate.Y_AXIS)
        );
        xRotate.angleProperty().bind(angleX);
        yRotate.angleProperty().bind(angleY);

        scene.setOnScroll((ScrollEvent ev)-> group.translateZProperty().set(group.getTranslateZ() - ev.getDeltaY()));

        scene.setOnMousePressed(event -> {
            klikX = event.getSceneX();
            klikY = event.getSceneY();
            klikAngleX = angleX.get();
            klikAngleY = angleY.get();
        });

        scene.setOnMouseDragged(event -> {
            angleX.set(klikAngleX - (klikY - event.getSceneY()));
            if( angleX.getValue() < 0) angleX.set(0);
            if( angleX.getValue() > 90) angleX.set(90);
            angleY.set(klikAngleY + klikX - event.getSceneX());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * GridPane obsahujuci buttony reprezentujuce podoris
     */
    public class PlaygroundPane extends GridPane {

        public PlaygroundPane() {
            setMinHeight(300);
            update();
        }
        public void update(){
            for (int riadok = 0; riadok < ROWS; riadok++) {
                for (int stlpec = 0; stlpec < ROWS; stlpec++) {
                    PlaygroundButton b = new PlaygroundButton();
                    poleGrid[riadok][stlpec]=b;
                    b.setOnMousePressed(b::click);
                    this.add(b, stlpec, riadok);
                }
            }
        }
    }


    /**
     * Button reprezentujuci podoris
     */
    public class PlaygroundButton extends Button{
        int value=0;

        PlaygroundButton(){
            setText(String.format("%02d", value));
        }

        void click(MouseEvent ev){
            r.setText(" ");
            r.setStyle("-fx-font-size: 20;");
            if (ev.getButton()==MouseButton.PRIMARY){
                value++;
            }else if(ev.getButton()==MouseButton.SECONDARY&&value>0){
                value--;
            }
            setText(String.format("%02d", value));
            if (value>0){ this.setStyle("-fx-background-color: red; ");
            } else {
                this.setStyle("");
            }
        }

        public void setValue(int value) {
            this.value = value;
            setText(String.format("%02d", value));
            if (value>0){ this.setStyle("-fx-background-color: red; ");
            } else {
                this.setStyle("");
            }
        }
    }


    /**
     * Box reprezentujuci kocku v stavebnici
     */
    public class PlaygroundBox extends Box{
        int value ;
        int column;
        int row;

        PlaygroundBox(int value, int row, int column, double tX, double tY, double tZ){
            super(20,20,20);
            this.value = value;
            this.row = row;
            this.column = column;
            setTranslateX(tX);
            setTranslateY(tY);
            setTranslateZ(tZ);
            setMaterial(boxMaterial);
            setOnMouseClicked(ev -> click(ev));
        }
        void click(MouseEvent ev) {
            r.setText(" ");
            r.setStyle(" -fx-font-size: 20;");
            if (poleDoska[row][column].size() > 0) {
                if (this.equals(poleDoska[row][column].get(poleDoska[row][column].size()-1))) {
                    if (ev.getButton() == MouseButton.PRIMARY) {
                        PlaygroundBox tmp = new PlaygroundBox(value+1,row,column,getTranslateX(),getTranslateY()-20,getTranslateZ());
                        poleDoska[row][column].add(tmp);
                        group.getChildren().add(tmp);
                    } else if (ev.getButton() == MouseButton.SECONDARY && poleDoska[row][column].size() > 1) {
                        poleDoska[row][column].remove(value);
                        group.getChildren().remove(this);
                    }
                }
            }
        }

    }
}
