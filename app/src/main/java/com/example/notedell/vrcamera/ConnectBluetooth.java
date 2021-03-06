package com.example.notedell.vrcamera;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.notedell.vrcamera.Threads.BluetoothThread;

import java.util.Set;


public class ConnectBluetooth extends ListActivity{

    private static final String TAG = "Connect Bluetooth";
    static BluetoothThread btt;
    private BluetoothAdapter btAdapter;

    public static int ENABLE_BLUETOOTH = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        Se existir uma conexão ativa ao iniciar essa Activity, a mesma será encerrada
         */

        if(btt != null) {
            btt.interrupt();
            btt = null;
            Log.d(TAG, "Connection Ended.");
        }


        /*
        Necessário para utilizar a ListActivity
         */
        ListView lv = getListView();
        LayoutInflater inflater = getLayoutInflater();
        View header = inflater.inflate(R.layout.act_paired_devices, lv, false);
        ((TextView) header.findViewById(R.id.txtValue)).setText("\nDispositivos pareados\n");
        lv.addHeaderView(header, null, false);

        /*  Usa o adaptador Bluetooth para obter uma lista de dispositivos pareados.
         */
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        if(!btAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, ENABLE_BLUETOOTH);
        }
        if(btAdapter.isEnabled()) {
            Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

        /*  Cria um modelo para a lista e o adiciona à tela.
            Se houver dispositivos pareados, adiciona cada um à lista.
         */
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
            setListAdapter(adapter);
            if (pairedDevices.size() > 0) {
                for (BluetoothDevice device : pairedDevices) {
                    adapter.add(device.getName() + "\n" + device.getAddress());
                }
            }
        }
    }



    /**
     * This Method is executed when the user click in listview
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {

        /*  Extrai nome e endereço a partir do conteúdo do elemento selecionado.
            Nota: position-1 é utilizado pois adicionamos um título à lista e o
        valor de position recebido pelo método é deslocado em uma unidade.
         */
        String item = (String) getListAdapter().getItem(position-1);
        String devName = item.substring(0, item.indexOf("\n"));
        String devAddress = item.substring(item.indexOf("\n") + 1, item.length());

        //Toast.makeText(getApplicationContext(),devAddress,Toast.LENGTH_SHORT).show();

        btt = new BluetoothThread(devAddress, new Handler() {

            @Override
            public void handleMessage(Message message) {

                String s = (String) message.obj;

                switch (s) {
                    case "CONNECTED": {
                        TextView tv = (TextView) findViewById(R.id.statusText);
                        tv.setText("Conectado.");

                        Toast.makeText(getApplicationContext(),"Conectado", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                        close();

                        break;
                    } case "DISCONNECTED": {
                        TextView tv = (TextView) findViewById(R.id.statusText);
                        tv.setText("Desconectado.");
                        getListView().setEnabled(true);
                        break;
                    }
                    case "CONNECTION FAILED": {
                        TextView tv = (TextView) findViewById(R.id.statusText);
                        tv.setText("Falha na Conexão!");
                        getListView().setEnabled(true);
                        btt = null;
                        break;
                    }
                }
            }
        });

        // Inicia a Thread
        btt.start();

        TextView tv = (TextView) findViewById(R.id.statusText);
        tv.setText("Conectando...");
        getListView().setEnabled(false);

    }


    /*
    Método que recebe a resposta do usuário ao ser invocado pelo startActivityForResult
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == ENABLE_BLUETOOTH) {
            switch (resultCode) {
                case RESULT_CANCELED:
                    Toast.makeText(getApplicationContext(), "Erro! Bluetooth não ativo!",
                            Toast.LENGTH_SHORT).show();
                    close();
                    break;

                case RESULT_OK:
                    if (btAdapter.isEnabled()) {
                        Set<BluetoothDevice> pairedDevices = btAdapter.getBondedDevices();

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
                        setListAdapter(adapter);
                        if (pairedDevices.size() > 0) {
                            for (BluetoothDevice device : pairedDevices) {
                                adapter.add(device.getName() + "\n" + device.getAddress());
                            }
                        }
                    }
                    break;
            }
        }

    }

    /*
    Fecha a Activity
     */
    public void close() {
        this.finish();
    }
}