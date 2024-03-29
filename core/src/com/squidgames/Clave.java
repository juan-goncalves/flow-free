package com.squidgames;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;

/**
 * Created by juan_ on 15-Jun-17.
 */

//TODO: Cambiar todo el trabajo con ShapeRenderer a Batch, usando texturas, por ahora necesitariamos una textura Circulo

public class Clave extends Casilla {
    private String TAG = getClass().getSimpleName();
    private float espacioCasilla;
    private ShapeRenderer renderer;

    public Clave(int tableroI, int tableroJ, Color color, float espacioCasilla, boolean extremo, ShapeRenderer renderer) {
        super();
        this.renderer = renderer;
        this.setI(tableroI);
        this.setJ(tableroJ);
        this.espacioCasilla = espacioCasilla;
        this.setOrigin(0,0);
        this.setCircle(new Circle(0,0,0));
        if (color != null)
            this.setColor(color);
        else
            this.setColor(new Color());

        this.setExtremo(extremo);
        this.addListener(new ClickListener(){
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                Vector2 basePosition = new Vector2(Gdx.input.getX(), Gdx.input.getY());
                Vector2 traduced = getStage().screenToStageCoordinates(basePosition);

                Gdx.app.log("IsConnected?", Clave.this.toString() + " - " + Clave.this.isConnected());
                Gdx.app.log(TAG,"Touch pos: " +traduced.x + "," + traduced.y + " PreTraduced: " + basePosition.x + "," + basePosition.y);

                Clave.this.setLastTouchCasilla(Clave.this);
                Clave.this.getLastTouchCasilla().liberar();
                if (Clave.this.getLastTouchCasilla().getPareja() != null){
                    Clave.this.getLastTouchCasilla().getPareja().liberar();
                    Gdx.app.log("LIBERAR", Clave.this.getLastTouchCasilla().getPareja().toString());
                }

                return true;
            }
        });
    }

    public void modifyOrigin(Vector2 newOrigin){
        this.setOrigin(newOrigin.x,newOrigin.y);
        this.setBounds(newOrigin.x,newOrigin.y,espacioCasilla,espacioCasilla);
        if (this.isExtremo())
            this.setTouchable(Touchable.enabled);
        else
            this.setTouchable(Touchable.disabled);

        float radio = calcularRadio();
        Vector2 posCirculo = new Vector2(this.getOriginX() + espacioCasilla/2,this.getOriginY() + espacioCasilla/2);

        if (this.getColor() != null) {
            this.setCircle(new Circle(posCirculo,radio));
        } else
            this.setCircle(new Circle(posCirculo,0));
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        renderer.setColor(this.getColor());
        renderer.begin(ShapeRenderer.ShapeType.Filled);
        renderer.circle(this.getCircle().x, this.getCircle().y, this.getCircle().radius, 100);
        renderer.end();

        if (this.getSucesor() != null) {
            renderer.begin(ShapeRenderer.ShapeType.Filled);
            renderer.setColor(this.getColor());
            renderer.rectLine(this.getOriginX()+ this.getWidth()/2,this.getOriginY()+ this.getWidth()/2,
                    getSucesor().getOriginX() + this.getWidth()/2,getSucesor().getOriginY() + this.getWidth()/2,this.getWidth()/3f);
            renderer.end();
        }
    }

    @Override
    public void ejecutarMovimiento(Casilla objetivo){
        if (objetivo != null) {
            Casilla lastTouchClave = this.getLastTouchCasilla();
            //No permitir movernos a claves extremos de diferente color
            if (objetivo.isExtremo() && lastTouchClave.getPareja() != null && !lastTouchClave.getColor().equals(objetivo.getColor())) {
                Gdx.app.log(TAG,"No te puedes mover a una clave de diferente color!");
                return;
            }

            if (lastTouchClave.isExtremo() && lastTouchClave.getPredecesor() != null) {
                Gdx.app.log(TAG,"Camino completado, levanta el dedo!");
                return;
            }


            boolean connect = false;
            if (objetivo.equals(this.getPareja())) {
                Gdx.app.log("completing", lastTouchClave.toString());
                //lastTouchClave.caminoCompleted();
                connect = true;
            }

            Gdx.app.log("MOVIMIENTO",String.format("Inicio: %s  Final: %s",this.toString(),objetivo.toString()));

            if (lastTouchClave.getPredecesor() != null && lastTouchClave.getPredecesor().equals(objetivo)) {
                lastTouchClave.desconectar();
            }else if (objetivo.getPredecesor() != null || objetivo.getSucesor() != null) {
                //Regresar e ir avanzndo por encima de otro camino
                objetivo.liberar();
                //INTERSECCION
                if (!lastTouchClave.getColor().equals(objetivo.getColor()) && objetivo.getPredecesor() != null){
                    objetivo.getPredecesor().liberar();
                    lastTouchClave.conectar(objetivo);
                }
            } else
                lastTouchClave.conectar(objetivo);

            this.setLastTouchCasilla(objetivo);
            objetivo.setTouchable(Touchable.enabled);
            //Si el camino fue completado
            if (connect)
                objetivo.caminoCompleted();
        }
    }

    public Casilla getObjetivo(Vector2 current) {
        //NOTA: El parametro current se considera traducido, es decir, se considera que ya esta pasado a coordenadas del stage
        Casilla lastTouchClave = this.getLastTouchCasilla();
        Casilla objetivo = null;

        if (current.x >= lastTouchClave.getOriginX() + espacioCasilla && current.y <= lastTouchClave.getOriginY() + espacioCasilla) {
            Gdx.app.log(TAG,"Saliendo por Lado 2");
            objetivo = lastTouchClave.getVecinos().get(2);
        }

        if (current.x < lastTouchClave.getOriginX() && current.y <= lastTouchClave.getOriginY() + espacioCasilla) {
            Gdx.app.log(TAG,"Saliendo por Lado 4");
            objetivo = lastTouchClave.getVecinos().get(4);
        }

        if (current.x <= lastTouchClave.getOriginX() + espacioCasilla
                && current.x >= lastTouchClave.getOriginX()
                && current.y > lastTouchClave.getOriginY() + espacioCasilla) {
            Gdx.app.log(TAG,"Saliendo por Lado 1");
            objetivo = lastTouchClave.getVecinos().get(1);
        }

        if (current.x <= lastTouchClave.getOriginX() + espacioCasilla
                && current.x >= lastTouchClave.getOriginX()
                && current.y < lastTouchClave.getOriginY()) {
            Gdx.app.log(TAG,"Saliendo por Lado 3");
            objetivo = lastTouchClave.getVecinos().get(3);
        }

        return objetivo;
    }

    @Override
    public  float calcularRadio() {
        float areaCasilla = espacioCasilla*espacioCasilla;
        //El area de los circulos es el 25% del area de la casilla
        float areaCirculo = (0.25f) * areaCasilla;
        //Despejamos el radio de la formula del area del circulo (PI*radio^2)
        float radio = (float)Math.sqrt(areaCirculo/Math.PI);
        return radio;
    }
}
