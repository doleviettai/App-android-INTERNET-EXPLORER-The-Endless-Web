package com.example.internet_explorer.app.data.local

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class CaseAssetLoader(
        private val context: Context,
        private val caseFolder: String = "case_vanished_network"
    ) {
        private val gson = Gson()
        val gsonInstance: Gson get() = gson

        private fun readAsset(fileName: String): String =
        context.assets.open("$caseFolder/$fileName").bufferedReader().use { it.readText() }

        fun loadCase(): CaseJson =
        gson.fromJson(readAsset("case.json"), CaseJson::class.java)

        fun loadOrganization(): OrganizationJson =
        gson.fromJson(readAsset("organization.json"), OrganizationJson::class.java)

        fun loadNpcs(): List<NpcJson> =
        gson.fromJson(readAsset("npcs.json"), object : TypeToken<List<NpcJson>>() {}.type)

        fun loadWebsites(): List<WebsiteJson> =
        gson.fromJson(readAsset("websites.json"), object : TypeToken<List<WebsiteJson>>() {}.type)

        fun loadWorldFacts(): List<WorldFactJson> =
        gson.fromJson(readAsset("world_facts.json"), object : TypeToken<List<WorldFactJson>>() {}.type)

        fun loadEmails(): List<EmailJson> =
        gson.fromJson(readAsset("emails.json"), object : TypeToken<List<EmailJson>>() {}.type)

        fun loadPuzzle(): PuzzleJson =
        gson.fromJson(readAsset("puzzle.json"), PuzzleJson::class.java)

        fun loadClues(): List<ClueJson> =
        gson.fromJson(readAsset("clues.json"), object : TypeToken<List<ClueJson>>() {}.type)
}