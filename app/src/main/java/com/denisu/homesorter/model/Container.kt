package com.denisu.homesorter.model

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import java.io.Serializable

data class Item(val nome: String, val id: Int) : Serializable

class Container(var nome: String, val id: Int, val containerFlag: Boolean) : Serializable {
    //val name = nome
    //val itens = mutableListOf<Item>()
    var ultimosParentes = mutableListOf<Int>() //apenas visivel se containerFlag for false
    //var imagePath = ""
    var description = ""

    val subContainers = mutableListOf<Container>() //containers devem remover todos os subcontainers para se tornar objeto

    fun adicionarItem(item: Item) {
        //itens.add(item)
    }

    fun contarSubContainers(): Int {
        var count = 0
        for (subContainer in subContainers) {
            count++
            count += subContainer.contarSubContainers()
        }
        return count
    }


    fun adicionarSubContainer(subContainer: Container, parentId: Int) {

        subContainer.ultimosParentes.add(parentId)
        if(subContainer.ultimosParentes.size == 5)
            subContainer.ultimosParentes.removeAt(0)

        subContainers.add(subContainer)
    }

    fun atualizarContainer(novoNome: String, novoDesc: String) {
        description = novoDesc
        nome = novoNome
    }

    fun excluirSubcontainer(container: Container) {
        subContainers.remove(container)
    }

    fun buscarSubContainerPorNome(nomeContainer: String): Container? {
        val subContainerEncontrado = subContainers.find { it.nome == nomeContainer }
        if (subContainerEncontrado != null) {
            Log.d("TAG", "SubContainer Encontrado: "+subContainerEncontrado.nome+" "+subContainerEncontrado.id)
            return subContainerEncontrado
        } else {
            for (subContainer in subContainers) {
                val subContainerEncontradoRecursivo = subContainer.buscarSubContainerPorNome(nomeContainer)
                if (subContainerEncontradoRecursivo != null) {
                    Log.d("TAG", "SubContainer Recursivo Encontrado: "+subContainerEncontradoRecursivo.nome+" "+subContainerEncontradoRecursivo.id)
                    return subContainerEncontradoRecursivo
                }
            }
            return null
        }
    }

    fun buscarSubContainerPorId(idContainer: Int): Container? {
        val subContainerEncontrado = subContainers.find { it.id == idContainer }
        if (subContainerEncontrado != null) {
            Log.d("TAG", "SubContainer Encontrado: "+subContainerEncontrado.nome+" "+subContainerEncontrado.id)
            return subContainerEncontrado
        } else {
            for (subContainer in subContainers) {
                Log.d("TAG", "Buscando no container: "+subContainer.nome)
                val subContainerEncontradoRecursivo = subContainer.buscarSubContainerPorId(idContainer)
                if (subContainerEncontradoRecursivo != null) {
                    Log.d("TAG", "SubContainer Recursivo Encontrado: "+subContainerEncontradoRecursivo.nome+" "+subContainerEncontradoRecursivo.id)
                    return subContainerEncontradoRecursivo
                }
            }
            return null
        }
    }
}