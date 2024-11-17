package com.example.t5a3_tortosa_pau.dao

import android.content.ContentValues
import android.database.Cursor
import com.example.t5a3_tortosa_pau.bd.MiBD
import com.example.t5a3_tortosa_pau.pojo.Cuenta
import com.example.t5a3_tortosa_pau.pojo.Movimiento
import java.lang.String
import java.util.Date
import kotlin.Any
import kotlin.Int
import kotlin.Long
import kotlin.arrayOf

class MovimientoDAO : PojoDAO {
    override fun add(obj: Any?): Long {
        val contentValues = ContentValues()
        val c: Movimiento = obj as Movimiento
        contentValues.put("tipo", c.getTipo())
        contentValues.put("fechaoperacion", c.getFechaOperacion()?.getTime() ?: null)
        contentValues.put("descripcion", c.getDescripcion())
        contentValues.put("importe", c.getImporte())
        contentValues.put("idcuentaorigen", c.getCuentaOrigen()?.getId() ?: -1)
        contentValues.put("idcuentadestino", c.getCuentaDestino()?.getId() ?: -1)
        return MiBD.dB?.insert("movimientos", null, contentValues) ?: -1
    }

    override fun update(obj: Any?): Int {
        val contentValues = ContentValues()
        val c: Movimiento = obj as Movimiento
        contentValues.put("tipo", c.getTipo())
        contentValues.put("fechaoperacion", c.getFechaOperacion()!!.getTime())
        contentValues.put("descripcion", c.getDescripcion())
        contentValues.put("importe", c.getImporte())
        contentValues.put("idcuentaorigen", c.getCuentaOrigen()!!.getId())
        contentValues.put("idcuentadestino", c.getCuentaDestino()!!.getId())
        val condicion = "id=" + String.valueOf(c.getId())
        return MiBD.dB?.update("movimientos", contentValues, condicion, null) ?: -1
    }

    override fun delete(obj: Any?) {
        val c: Movimiento = obj as Movimiento
        val condicion = "id=" + String.valueOf(c.getId())

        //Se borra el producto indicado en el campo de texto
        MiBD.dB?.delete("movimientos", condicion, null)
    }

    override fun search(obj: Any?): Any? {
        val c: Movimiento = obj as Movimiento
        val condicion = "id=" + String.valueOf(c.getId())
        val columnas = arrayOf(
            "id",
            "tipo",
            "fechaoperacion",
            "descripcion",
            "importe",
            "idcuentaorigen",
            "idcuentadestino"
        )
        val cursor: Cursor? =
            MiBD.dB?.query("movimientos", columnas, condicion, null, null, null, null) ?: null
        return if (cursor?.moveToFirst() == true) {
            c.setId(cursor.getInt(0))
            c.setTipo(cursor.getInt(1))
            c.setFechaOperacion(Date(cursor.getLong(2)))
            c.setDescripcion(cursor.getString(3))
            c.setImporte(cursor.getFloat(4))

            // Asignamos la cuenta de origen
            val a = Cuenta()
            a.setId(cursor.getInt(5))
            c.setCuentaOrigen(MiBD.getInstance(null)?.cuentaDAO?.search(a) as Cuenta)

            // Asignamos la cuenta de destino
            val aux = cursor.getInt(6)
            if (aux == -1) {
                a.setId(-1)
            } else {
                a.setId(aux)
                c.setCuentaOrigen(MiBD.getInstance(null)?.cuentaDAO?.search(a) as Cuenta)
            }
            c
        } else {
            null
        }
    }

    override fun getAll(): ArrayList<*> {
        val listaMovimientos: ArrayList<Movimiento> = ArrayList<Movimiento>()
        val columnas = arrayOf(
            "id",
            "tipo",
            "fechaoperacion",
            "descripcion",
            "importe",
            "idcuentaorigen",
            "idcuentadestino"
        )
        val cursor: Cursor? =
            MiBD.dB?.query("movimientos", columnas, null, null, null, null, null) ?: null
        if (cursor?.moveToFirst() == true) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                val c = Movimiento()
                c.setId(cursor.getInt(0))
                c.setTipo(cursor.getInt(1))
                c.setFechaOperacion(Date(cursor.getLong(2)))
                c.setDescripcion(cursor.getString(3))
                c.setImporte(cursor.getFloat(4))

                // Asignamos la cuenta de origen
                var a = Cuenta()
                a.setId(cursor.getInt(5))
                c.setCuentaOrigen(MiBD.getInstance(null)?.cuentaDAO?.search(a) as Cuenta)


                // Asignamos la cuenta de destino
                a = Cuenta()
                val aux = cursor.getInt(6)
                if (aux == -1) {
                    a.setId(-1)
                    c.setCuentaOrigen(a)
                } else {
                    a.setId(aux)
                    c.setCuentaDestino(MiBD.getInstance(null)?.cuentaDAO?.search(a) as Cuenta)
                }
                listaMovimientos.add(c)
            } while (cursor.moveToNext())
        }
        return listaMovimientos
    }

    fun getMovimientos(cuenta: Cuenta?): ArrayList<*> {
        val listaMovimientos: ArrayList<Movimiento> = ArrayList<Movimiento>()
        val condicion = "idcuentaorigen=" + String.valueOf(cuenta?.getId() ?: -1)
        val columnas = arrayOf(
            "id",
            "tipo",
            "fechaoperacion",
            "descripcion",
            "importe",
            "idcuentaorigen",
            "idcuentadestino"
        )
        val cursor: Cursor? =
            MiBD.dB?.query("movimientos", columnas, condicion, null, null, null, null) ?: null
        if (cursor?.moveToFirst() == true) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                val c = Movimiento()
                c.setId(cursor.getInt(0))
                c.setTipo(cursor.getInt(1))
                c.setFechaOperacion(Date(cursor.getLong(2)))
                c.setDescripcion(cursor.getString(3))
                c.setImporte(cursor.getFloat(4))

                // Asignamos la cuenta de origen
                c.setCuentaOrigen(cuenta)


                // Asignamos la cuenta de destino
                var a = Cuenta()
                val aux = cursor.getInt(6)
                if (aux == -1) {
                    a.setId(-1)
                    c.setCuentaDestino(a)
                } else {
                    a.setId(aux)
                    a = MiBD.getInstance(null)?.cuentaDAO?.search(a) as Cuenta
                    c.setCuentaDestino(a)
                }
                listaMovimientos.add(c)
            } while (cursor.moveToNext())
        }
        return listaMovimientos
    }

    fun getMovimientosTipo(cuenta: Cuenta?, tipo: Int): ArrayList<*> {
        val listaMovimientos: ArrayList<Movimiento> = ArrayList<Movimiento>()
        val condicion =
            "idcuentaorigen=" + String.valueOf(cuenta?.getId() ?: -1) + " AND tipo = " + tipo.toString()
        val columnas = arrayOf(
            "id",
            "tipo",
            "fechaoperacion",
            "descripcion",
            "importe",
            "idcuentaorigen",
            "idcuentadestino"
        )
        val cursor: Cursor? =
            MiBD.dB?.query("movimientos", columnas, condicion, null, null, null, null) ?: null
        if (cursor?.moveToFirst() == true) {
            //Recorremos el cursor hasta que no haya más registros
            do {
                val c = Movimiento()
                c.setId(cursor.getInt(0))
                c.setTipo(cursor.getInt(1))
                c.setFechaOperacion(Date(cursor.getLong(2)))
                c.setDescripcion(cursor.getString(3))
                c.setImporte(cursor.getFloat(4))

                // Asignamos la cuenta de origen
                c.setCuentaOrigen(cuenta)


                // Asignamos la cuenta de destino
                var a = Cuenta()
                val aux = cursor.getInt(6)
                if (aux == -1) {
                    a.setId(-1)
                    c.setCuentaDestino(a)
                } else {
                    a.setId(aux)
                    a = MiBD.getInstance(null)?.cuentaDAO?.search(a) as Cuenta
                    c.setCuentaDestino(a)
                }
                listaMovimientos.add(c)
            } while (cursor.moveToNext())
        }
        return listaMovimientos
    }
}