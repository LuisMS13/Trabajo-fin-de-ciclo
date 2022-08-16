package com.dam.protectofindeciclo.proyectotfc.ui.Juegos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.dam.protectofindeciclo.proyectotfc.model.Genre;
import com.dam.protectofindeciclo.proyectotfc.model.JuegoDetalles;
import com.dam.protectofindeciclo.proyectotfc.model.JuegosBusqueda;
import com.dam.protectofindeciclo.proyectotfc.model.Platform;
import com.dam.protectofindeciclo.proyectotfc.model.ResultsDetalles;
import com.dam.protectofindeciclo.proyectotfc.retrofit.APIRestService;
import com.dam.protectofindeciclo.proyectotfc.retrofit.RetrofitClient;
import com.dam.proyectotfc.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class DatosJuegoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "FALLO";
    TextView tvNombre;
    ImageView ivPortada;
    TextView tvDes;
    TextView tvGen;
    TextView tvPlat;
    String juegoGuid;
    Integer juegoId;
    FloatingActionButton btnEstadoJuego;
    String genero = "";
    String plataforma = "";
    RadioButton rdbJug, rdbCom, rdbAba, rdbOlv;
    RadioGroup rdnGroup;

    FirebaseDatabase fdb;
    DatabaseReference dbRef;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    ResultsDetalles juegoD;

    public DatosJuegoFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater
                .inflate(R.layout.fragment_datos_juego, container, false);


        Bundle bundle = this.getArguments();

        JuegosBusqueda info = bundle.getParcelable(JuegosInfoFragment.CLAVE_JUEGO);

        juegoGuid = info.getGuid();
        juegoId = info.getId();

        tvNombre = v.findViewById(R.id.tvNombreJuegoDetalle);
        tvDes = v.findViewById(R.id.tvDescripcionJuegoDetalle);
        tvGen = v.findViewById(R.id.tvGeneroJuegoDetalle);
        tvPlat = v.findViewById(R.id.tvPlataformasJuegoDetalle);
        ivPortada = v.findViewById(R.id.ivPortadaJuegoDetalle);
        btnEstadoJuego = v.findViewById(R.id.fabEstadoJuego);
        btnEstadoJuego.setOnClickListener(this);


        fdb = FirebaseDatabase.getInstance();
        dbRef = fdb.getReference("usuarios");
        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();

        Retrofit r = RetrofitClient.getClient(APIRestService.BASE_URL);
        APIRestService ars = r.create(APIRestService.class);

        Call<JuegoDetalles> call = ars.getInfo(juegoGuid,APIRestService.KEY, APIRestService.FIELD_LIST,
                APIRestService.FORMAT);

        call.enqueue(new Callback<JuegoDetalles>() {
            @Override
            public void onResponse(Call<JuegoDetalles> call, Response<JuegoDetalles> response) {
                if (!response.isSuccessful()) {
                    Log.i(TAG, "error" + response.code());
                } else {

                   juegoD = response.body().getResults();
                   tvNombre.setText(juegoD.getName());
                   Glide.with(ivPortada.getContext()).load(juegoD.getImage().getMediumUrl()).into(ivPortada);
                   if (juegoD.getDescription() == null) {
                       tvDes.setText(R.string.no_descripcion);
                   }else {
                       tvDes.setText(Html.fromHtml(juegoD.getDescription()));
                   }
                    if (juegoD.getGenres() == null){
                        tvGen.setText(R.string.no_genero);
                    }else {
                        for (Genre gen: juegoD.getGenres()) {
                            genero += gen.getName() + "\t";
                        }
                        tvGen.setText(String.format(getResources().getString(R.string.tv_genero_juego_detalle),genero));
                    }
                    for (Platform plat: juegoD.getPlatforms()) {
                        plataforma += plat.getName() + ",\t";
                    }
                    tvPlat.setText(String.format(getResources().getString(R.string.tv_plataformas_juego_detalle),plataforma));
                }
            }

            @Override
            public void onFailure(Call<JuegoDetalles> call, Throwable t) {
                Log.e(TAG ,"he fallao" + t.toString());
            }
        });


        return v;
    }

    @Override
    public void onClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View viewD = getActivity().getLayoutInflater().inflate(R.layout.dialog_estado_layout, null);
        builder.setView(viewD);
        builder.setTitle(R.string.title_estado_juego_dialog);
        rdnGroup = viewD.findViewById(R.id.rdgEstadoJuegos);
        rdbJug = viewD.findViewById(R.id.rdbJugado);
        rdbCom = viewD.findViewById(R.id.rdbCompletado);
        rdbAba = viewD.findViewById(R.id.rdbAbandonado);
        rdbOlv = viewD.findViewById(R.id.rdbOlvidado);
        builder.setPositiveButton(R.string.dialog_ok,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                validar();
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.dialog_ok_no,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                dialog.dismiss();
            }
        });

        builder.create().show();

    }

    private void validar() {
        String id = fUser.getUid();
        if (rdbJug.isChecked()) {
            dbRef.child(id).child("listaJugados").child(String.valueOf(juegoId)).setValue(juegoD.getName());
        } else if (rdbCom.isChecked()) {
            dbRef.child(id).child("listaCompletados").child(String.valueOf(juegoId)).setValue(juegoD.getName());
        } else if (rdbAba.isChecked()) {
            dbRef.child(id).child("listaMedias").child(String.valueOf(juegoId)).setValue(juegoD.getName());
        } else if (rdbOlv.isChecked()) {
            dbRef.child(id).child("listaOlvidados").child(String.valueOf(juegoId)).setValue(juegoD.getName());
        }
        Toast.makeText(getContext(), getString(R.string.msj_juego_est), Toast.LENGTH_LONG).show();
    }
}