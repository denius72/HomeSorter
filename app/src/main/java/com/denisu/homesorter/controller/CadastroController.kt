package com.denisu.homesorter.controller

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.denisu.homesorter.model.Container
import com.denisu.homesorter.model.Containers
import com.denisu.homesorter.model.Item
import com.denisu.homesorter.sharedPreferences
import com.google.gson.Gson
import java.io.Serializable
import kotlin.math.log

class CadastroController (private val containers: MutableList<Container>) : Serializable {
    //private val rootContainer = Container("Root", 0)
    private var contadorId = 1

    fun cadastrarContainer(nome: String, containerId: Int, containerFlag: Boolean) {
        Log.d("TAG", "Trabalhando com a instância de container $containers")
        val container = buscarContainerPorId(containerId)
        if (container != null) {
            val novoContainer = Container(nome, gerarNovoId(), containerFlag)
            container.adicionarSubContainer(novoContainer, containerId)
        } else {
            val container = buscarSubcontainerPorId(containerId)
            if (container != null) {
                val novoContainer = Container(nome, gerarNovoId(), containerFlag)
                container.adicionarSubContainer(novoContainer, containerId)
            } else {
                Log.d("TAG", "Container não encontrado.")
            }
        }
    }

    fun editarContainer(nome: String, description: String,containerId: Int) {
        Log.d("TAG", "Trabalhando com a instância de container $containers")
        val container = buscarContainerPorId(containerId)
        if (container != null) {
            container.atualizarContainer(nome, description)
        } else {
            val container = buscarSubcontainerPorId(containerId)
            if (container != null) {
                container.atualizarContainer(nome, description)
            } else {
                Log.d("TAG", "Container não encontrado.")
            }
        }
    }

    fun searchContainers(query: String): List<Pair<String, Container>> {
        val searchResults = mutableListOf<Pair<String, Container>>()
        searchContainersRecursively(containers[0], query, mutableListOf(), searchResults)
        return searchResults
    }

    private fun searchContainersRecursively(container: Container, query: String, path: MutableList<String>, searchResults: MutableList<Pair<String, Container>>) {
        path.add(container.nome)

        if (container.nome.contains(query, true)) {
            searchResults.add(path.joinToString(separator = " -> ") to container)
        }

        container.subContainers.forEach { subContainer ->
            searchContainersRecursively(subContainer, query, path.toMutableList(), searchResults)
        }
    }

    fun searchContainerPathById(id: Int): String? {
        return searchContainerPathRecursivelyById(containers[0], id, mutableListOf())
    }

    private fun searchContainerPathRecursivelyById(container: Container, id: Int, path: MutableList<String>): String? {
        path.add(container.nome)

        if (container.id == id) {
            return path.joinToString(separator = " -> ")
        }

        container.subContainers.forEach { subContainer ->
            val result = searchContainerPathRecursivelyById(subContainer, id, path.toMutableList())
            if (result != null) {
                return result
            }
        }
        //se não achou nada
        return null
    }



    fun searchContainersToMove(containerId: Int, query: String): List<Pair<String, Container>> {
        val searchResults = mutableListOf<Pair<String, Container>>()
        searchContainersToMoveRecursively(containers[0], query, mutableListOf(), searchResults, containerId)
        return searchResults
    }

    private fun searchContainersToMoveRecursively(container: Container, query: String, path: MutableList<String>, searchResults: MutableList<Pair<String, Container>>, avoidContainerId: Int) {
        if (container.id != avoidContainerId) {
            path.add(container.nome)

            if (container.nome.contains(query, true) && container.containerFlag) {
                searchResults.add(path.joinToString(separator = " -> ") to container)
            }

            container.subContainers.forEach { subContainer ->
                searchContainersToMoveRecursively(subContainer, query, path.toMutableList(), searchResults, avoidContainerId)
            }
        }
    }

    fun buscarContainerPorId(id: Int): Container? {
        return containers.find { it.id == id }
    }

    fun buscarContainerPorNome(nome: String): Container? {
        return containers.find { it.nome == nome }
    }

    fun buscarSubcontainerPorId(id: Int): Container? {
        return containers[0].buscarSubContainerPorId(id)
    }

    fun buscarSubcontainerPorNome(nome: String): Container? {
        return containers[0].buscarSubContainerPorNome(nome)
    }

    fun excluirSubcontainerPorId(id: Int, parentId: Int) {
        val sub = containers[0].buscarSubContainerPorId(id)
        var parent = containers[0].buscarSubContainerPorId(parentId)
        if(parent == null)
            parent = containers[0]
        if (sub != null) {
            //sub.itens.clear()
            sub.subContainers.clear()
            parent!!.excluirSubcontainer(sub)
        }
    }

    private fun gerarNovoId(): Int {
        //todo SUBSTITUIR POR SQLITE
        //return Containers.database.gerarNovoId() //direto do SQLite

        Containers.novoid++
        val editor = sharedPreferences.edit() //da classe principal
        editor.putInt("novoId", Containers.novoid)
        editor.apply()
        return Containers.novoid

    }

    fun moverContainer(containerToMove: Container, containerDestination: Container, containerPai: Int): Boolean {
        if (containerToMove.id == containerDestination.id) {
            return false
        }

        if (isDescendant(containerToMove, containerDestination)) {
            return false
        }

        val containerPai = buscarSubcontainerPorId(containerPai)
        val gson = Gson()

        Log.d("TAG2", ""+containerPai?.subContainers?.remove(containerToMove))
        Log.d("TAG2", "containerPai "+gson.toJson(containerPai?.subContainers))
        if(containerPai?.id == null)
            excluirSubcontainerPorId(containerToMove.id,0)
        else
            excluirSubcontainerPorId(containerToMove.id,containerPai!!.id)
        Log.d("TAG2", "containerPai "+gson.toJson(containerPai?.subContainers))

        containerToMove.ultimosParentes.add(containerDestination.id)
        if(containerToMove.ultimosParentes.size == 5)
            containerToMove.ultimosParentes.removeAt(0)

        containerDestination.subContainers.add(containerToMove)

        return true
    }

    private fun isDescendant(containerToMove: Container, containerDestination: Container): Boolean {
        if (containerDestination == containerToMove) {
            return true
        }
        return containerToMove.subContainers.any { isDescendant(it, containerDestination) }
    }

}