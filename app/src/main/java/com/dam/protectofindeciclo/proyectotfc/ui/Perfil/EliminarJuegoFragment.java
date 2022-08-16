package com.dam.protectofindeciclo.proyectotfc.ui.Perfil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.dam.protectofindeciclo.proyectotfc.model.Genre;
import com.dam.protectofindeciclo.proyectotfc.model.JuegoDetalles;
import com.dam.protectofindeciclo.proyectotfc.model.Platform;
import com.dam.protectofindeciclo.proyectotfc.model.ResultEstado;
import com.dam.protectofindeciclo.proyectotfc.model.ResultsDetalles;
import com.dam.protectofindeciclo.proyectotfc.retrofit.APIRestService;
import com.dam.protectofindeciclo.proyectotfc.retrofit.RetrofitClient;
import com.dam.proyectotfc.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class EliminarJuegoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "FALLO";
    TextView tvNombre;
    ImageView ivPortada;
    TextView tvDes;
    TextView tvGen;
    TextView tvPlat;
    Integer juegoId;
    FloatingActionButton btnEliminarJuego;
    String genero = "";
    String plataforma = "";

    FirebaseDatabase fdb;
    DatabaseReference dbRef;
    FirebaseAuth fAuth;
    ResultsDetalles juegoE;
    String juegoGuid;
    String estado;
    String idUser;
    ValueEventListener vel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_eliminar_juego, container, false);

        Bundle bundle = this.getArguments();

        ResultEstado info = bundle.getParcelable(EstadoJuegoFragment.CLAVE_JUEGO_E);
        estado = bundle.getString(EstadoJuegoFragment.CLAVE_ESTADO);

        juegoGuid = info.getGuid();
        juegoId = info.getId() ;

        tvNombre = v.findViewById(R.id.tvNombreJuegoEliminar);
        tvDes = v.findViewById(R.id.tvDescripcionJuegoEliminar);
        tvGen = v.findViewById(R.id.tvGeneroJuegoEliminar);
        tvPlat = v.findViewById(R.id.tvPlataformasJuegoEliminar);
        ivPortada = v.findViewById(R.id.ivPortadaJuegoEliminar);
        btnEliminarJuego = v.findViewById(R.id.fabEliminarJuego);
        btnEliminarJuego.setOnClickListener(this);

        fdb = FirebaseDatabase.getInstance();
        dbRef = fdb.getReference("usuarios");
        fAuth = FirebaseAuth.getInstance();
        idUser = fAuth.getCurrentUser().getUid();

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
                juegoE = response.body().getResults();
                tvNombre.setText(juegoE.getName());
                Glide.with(ivPortada.getContext()).load(juegoE.getImage().getMediumUrl()).into(ivPortada);
                if (juegoE.getDescription() == null) {
                    tvDes.setText(R.string.no_descripcion);
                } else {
                    tvDes.setText(Html.fromHtml(juegoE.getDescription()));
                }
                if (juegoE.getGenres() == null){
                    tvGen.setText(R.string.no_genero);
                } else {
                    for (Genre gen: juegoE.getGenres()) {
                        genero += gen.getName() + "\t";
                    }
                    tvGen.setText(String.format(getResources().getString(R.string.tv_genero_juego_detalle),genero));
                }
                for (Platform plat: juegoE.getPlatforms()) {
                    plataforma += plat.getName() + ",\t";
                }
                tvPlat.setText(String.format(getResources().getString(R.string.tv_plataformas_juego_detalle),plataforma));
            }
        }
            @Override
            public void onFailure(Call<JuegoDetalles> call, Throwable t) {
                Log.i(TAG, "error" + t.toString());
            }
        });
        return v;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (vel != null){
            dbRef.removeEventListener(vel);
        }

    }

    @Override
    public void onClick(View view) {
        if (estado == "j") {
            dbRef = fdb.getReference("/usuarios/"+idUser+"/listaJugados");
            addValueEventListener();
        } else if (estado == "c") {
            dbRef = fdb.getReference("/usuarios/"+idUser+"/listaCompletados");
            addValueEventListener();
        }else if (estado == "m") {
            dbRef = fdb.getReference("/usuarios/"+idUser+"/listaMedias");
            addValueEventListener();
        }else if (estado == "o") {
            dbRef = fdb.getReference("/usuarios/"+idUser+"/listaOlvidados");
            addValueEventListener();
        }
    }

    private void addValueEventListener() {
        if (vel == null) {
            vel = new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.getValue() == null) {

                    }else {
                        HashMap<String, String> valoresJuegos = (HashMap<String, String>) snapshot.getValue();
                        Set<String> keySet = valoresJuegos.keySet();
                        ArrayList<String> juegoP = new ArrayList<String>(keySet);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        View viewD = getActivity().getLayoutInflater().inflate(R.layout.dialog_eliminar_layout, null);
                        builder.setView(viewD);
                        builder.setTitle(R.string.title_eliminar_juego_dialog);
                        builder.setPositiveButton(R.string.dialog_ok,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int j) {
                                        for (int i = 0; i < juegoP.size(); i++) {
                                            if (juegoP.get(i).equals(String.valueOf(juegoId))) {
                                                dbRef.child(String.valueOf(juegoId)).removeValue();
                                                dialog.dismiss();
                                                FragmentTransaction ft = getParentFragment().getChildFragmentManager().beginTransaction();
                                                PerfilFragment datos = new PerfilFragment();
                                                ft.replace(getId(),datos);
                                                ft.addToBackStack(null);
                                                ft.setReorderingAllowed(true);
                                                ft.commit();
                                            }
                                        }
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
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            };
        }
        dbRef.addValueEventListener(vel);
    }
}