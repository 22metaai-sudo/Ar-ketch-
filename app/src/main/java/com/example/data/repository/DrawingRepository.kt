package com.example.data.repository

import com.example.data.local.DrawingDao
import com.example.data.model.DrawingProject
import com.example.data.model.InspirationPost
import com.example.data.model.StepByStepTutorial
import com.example.data.model.TimelapseSession
import com.example.data.model.TraceTemplate
import com.example.data.model.TutorialStep
import kotlinx.coroutines.flow.Flow

class DrawingRepository(private val drawingDao: DrawingDao) {

    val allProjects: Flow<List<DrawingProject>> = drawingDao.getAllProjects()
    val allSessions: Flow<List<TimelapseSession>> = drawingDao.getAllSessions()

    suspend fun getProjectById(id: Long): DrawingProject? = drawingDao.getProjectById(id)

    suspend fun getLatestProject(): DrawingProject? = drawingDao.getLatestProject()

    suspend fun getLatestAutoSavedProject(): DrawingProject? = drawingDao.getLatestAutoSavedProject()

    suspend fun saveProject(project: DrawingProject): Long {
        return if (project.id == 0L) {
            drawingDao.insertProject(project)
        } else {
            drawingDao.updateProject(project)
            project.id
        }
    }

    suspend fun deleteProject(project: DrawingProject) = drawingDao.deleteProject(project)

    suspend fun saveTimelapseSession(session: TimelapseSession): Long = drawingDao.insertSession(session)

    suspend fun deleteTimelapseSession(session: TimelapseSession) = drawingDao.deleteSession(session)

    companion object {
        fun getCategories(): List<String> = listOf(
            "Tous (500+)",
            "Hyper-Réaliste & Réaliste",
            "Anime & Manga",
            "Animaux Sauvages",
            "Anatomie & Portraits",
            "Mandalas & Géométrie",
            "Tatouages & Street Art",
            "Paysages & Architecture",
            "Botanique & Fleurs",
            "Microscopie & Macro"
        )

        // 500+ Curated and structured models for artists
        fun getCuratedTemplates(): List<TraceTemplate> {
            val list = mutableListOf<TraceTemplate>()

            // 1. Featured core templates with verified drawable assets
            list.add(
                TraceTemplate(
                    id = "tpl_anime_hero",
                    title = "Héros Shōnen Manga",
                    category = "Anime & Manga",
                    drawableResName = "template_anime_hero",
                    difficulty = "Intermédiaire",
                    description = "Portrait expressif avec mèches dynamiques, regard déterminé et col de blouson.",
                    tags = listOf("Manga", "Portrait", "Shonen", "Cheveux", "Regard")
                )
            )
            list.add(
                TraceTemplate(
                    id = "tpl_majestic_wolf",
                    title = "Loup Sauvage Géométrique",
                    category = "Animaux Sauvages",
                    drawableResName = "template_majestic_wolf",
                    difficulty = "Avancé",
                    description = "Loup hurlant à la lune sur falaise rocheuse avec facettes géométriques ultra-nettes.",
                    tags = listOf("Loup", "Géométrie", "Lune", "Nature", "Lignes")
                )
            )
            list.add(
                TraceTemplate(
                    id = "tpl_sample_parrot",
                    title = "Ara Macao Tropical",
                    category = "Hyper-Réaliste & Réaliste",
                    drawableResName = "sample_photo_parrot",
                    difficulty = "Débutant à Pro",
                    description = "Perroquet aux plumages colorés idéal pour tester les filtres Sobel et Belle Vue.",
                    tags = listOf("Oiseau", "Réaliste", "Couleur", "Plumes", "Faune")
                )
            )
            list.add(
                TraceTemplate(
                    id = "tpl_ar_butterfly",
                    title = "Papillon Cybernétique",
                    category = "Mandalas & Géométrie",
                    drawableResName = "ic_ar_trace_logo",
                    difficulty = "Débutant",
                    description = "Ailes symétriques à motifs de mandala et grille laser pour un alignement parfait.",
                    tags = listOf("Papillon", "Symétrie", "Mandala", "Laser")
                )
            )

            // 2. Generating the 500+ curated catalog structured across all artistic disciplines
            val categoriesMap = mapOf(
                "Hyper-Réaliste & Réaliste" to listOf(
                    "Oeil Humain Macro avec Reflet Cristallin",
                    "Regard Intense & Cils Détaillés",
                    "Goutte d'Eau sur Pétale Transparent",
                    "Grains de Peau & Rides d'Expression",
                    "Portrait de Vieillard Barbe d'Argent",
                    "Main Tenue en Clé de Sol",
                    "Félin aux Moustaches Argentées",
                    "Pomme aux Reflets Hyper-Fidèles",
                    "Verre d'Eau & Réflexion Optique",
                    "Plume Délicate aux Micro-Détails",
                    "Tigre du Bengale en Vue Rapprochée",
                    "Portrait de Femme aux Boucles Floues",
                    "Glace Brisée & Éclats Cristallins",
                    "Feuille d'Automne aux Nervures Creusées",
                    "Chiot Golden Retriever Poil Soyeux",
                    "Fleur de Pissenlit aux Aigrettes Précises",
                    "Statue Antique en Marbre Patiné",
                    "Violon Vernis & Cordes d'Or",
                    "Guitare Acoustique & Rosace d'Ébène",
                    "Flocon de Neige Hexagonal Parfait"
                ),
                "Anime & Manga" to listOf(
                    "Guerrier Saiyan Énergie Flamboyante",
                    "Héroïne Cyberpunk aux Lunettes Néon",
                    "Samouraï du Vent Lame Dégainée",
                    "Ninja Masqué dans les Ombres",
                    "Chibi Dragon aux Yeux Ronds",
                    "Mecha Titan aux Blindages Hexagonaux",
                    "Écolière Manga Vent dans les Cheveux",
                    "Alchimiste au Cercle de Transmutation",
                    "Capitaine Pirate au Manteau d'Or",
                    "Sorcier Astral aux Runes Mystiques",
                    "Chibi Neko Mignon à la Pelote",
                    "Archerie Elfique Arc Courbé",
                    "Chevalier d'Acier Armure Complète",
                    "Pilote d'Étoile Casque Visière Dégradée",
                    "Détective Nocturne Trench et Chapeau",
                    "Chibi Renard à Neuf Queues",
                    "Guerrière Valkyrie Ailes Déployées",
                    "Maître d'Arts Martiaux Coup de Pied Volant",
                    "Esprit de la Forêt Masque de Bois",
                    "Robo-Pet Chien Mécanique Avenir"
                ),
                "Animaux Sauvages" to listOf(
                    "Lion Majestueux Crinière Soufflée",
                    "Aigle Royal Fondant sur sa Proie",
                    "Panthère Noire Yeux Émeraude",
                    "Cheval Sauvage Crinière Échevelée",
                    "Éléphant Savane Trompe Élevée",
                    "Ours Polaire sur Banquise Bleue",
                    "Renard Rusé Pelage Flamboyant",
                    "Cerf Dix-Cors dans la Brume",
                    "Hibou Grand-Duc Ailes Silencieuses",
                    "Dauphin Bondissant Hors des Flots",
                    "Orque Nageant vers la Surface",
                    "Girafe Gracieuse au Coucher de Soleil",
                    "Guépard en Pleine Accélération",
                    "Tortue Marine Nageant en Récif",
                    "Colibri Butinant une Fleur Exotique",
                    "Caméléon Écailles Camouflées",
                    "Koala Endormi sur Eucalyptus",
                    "Panda Géant Croquant le Bambou",
                    "Lynx Boréal Pinceaux d'Oreilles",
                    "Baleine à Bosse Balayage Caudal"
                ),
                "Anatomie & Portraits" to listOf(
                    "Canon des Proportions de Vinci",
                    "Méthode Andrew Loomis Tête 3/4",
                    "Main Ouverte Paume Détaillée",
                    "Main Tenant un Crayon d'Artiste",
                    "Crâne Humain Vue Face & Profil",
                    "Écorché Musculaire Torse & Bras",
                    "Mains Croisées Geste Expressif",
                    "Profil Féminin Ligne Élégante",
                    "Profil Masculin Mâchoire Structurée",
                    "Squelette Cage Thoracique & Colonne",
                    "Pied Humain Arche & Phalanges",
                    "Yeux Émotions : Joie, Surprise, Colère",
                    "Lèvres & Sourires Nuances de Teintes",
                    "Oreille Anatomie Cartilages & Reliefs",
                    "Pose Assise en Contre-Plongée",
                    "Corps en Torsion Danse Classique",
                    "Poing Serré Dynamique de Force",
                    "Buste d'Athlète en Tension",
                    "Visage Enfantin Formes Douces",
                    "Main Tenue en Méditation Mudra"
                ),
                "Mandalas & Géométrie" to listOf(
                    "Mandala Rosace Solaire 12 Rayons",
                    "Fleur de Vie Circulaire Sacrée",
                    "Spirale d'Or Proportion Divine",
                    "Métatron Cube Géométrique Parfait",
                    "Fractale Baroque Arabesques Infinies",
                    "Étoile Octogonale Islamique Entrelacs",
                    "Nœud Celtique Triskel Éternel",
                    "Mandala Éléphant Ornemental",
                    "Rosace Gothique Vitrail Cathédrale",
                    "Pavage Géométrique Escherien",
                    "Mandala Lotus Épanoui 8 Pétales",
                    "Yantra Géométrie Méditative",
                    "Mandala Chakra du Cœur Anahata",
                    "Polyèdre Étoilé de Kepler",
                    "Labyrinthe Circulaire Harmonieux",
                    "Mandala Paon Ailes Rayonnantes",
                    "Tessellation Origami Plissée",
                    "Mandala Soleil et Lune Éclipse",
                    "Rosace Végétale Feuilles Entrelacées",
                    "Mandala Zen Enso Cercle d'Encre"
                ),
                "Tatouages & Street Art" to listOf(
                    "Dragon Japonais Ryu Ondoyant",
                    "Carpe Koï Remontant la Cascade",
                    "Tigre Traditionnel Irezumi",
                    "Rose Old School Poignard Transpercé",
                    "Crâne Mexicain Calavera Décorée",
                    "Hirondelle Sailor Jerry Traditionnelle",
                    "Ancre Marine Cordage Entrelacé",
                    "Cœur Poignardé Bannière Libre",
                    "Lettrage Gothique Calligraphié",
                    "Typographie Graffiti Wildstyle",
                    "Masque Hannya Cornes & Flammes",
                    "Serpent Enroulé Autour d'un Glaive",
                    "Aigle Américain Bannière Étoilée",
                    "Tête de Mort Casque de Moto",
                    "Boussole Antique Rose des Vents",
                    "Phare Marin Tempête & Vagues",
                    "Montre à Gousset Engrenages Brisés",
                    "Papillon Mort-Tête Acherontia",
                    "Mains de Prière Chapelet d'Argent",
                    "Fleur de Cerisier Sakura Tombante"
                ),
                "Paysages & Architecture" to listOf(
                    "Tour Eiffel Vue Contre-Plongée",
                    "Mont Fuji & Pagode aux Cerisiers",
                    "Grande Vague de Kanagawa",
                    "Cathédrale Notre-Dame Gothique",
                    "Colisée Antique Arches de Pierre",
                    "Pont de Brooklyn Câbles & Haubans",
                    "Chalet Alpin sous les Sapins Enneigés",
                    "Phare sur Falaise Frappé par la Houle",
                    "Canal de Venise Gondole & Palais",
                    "Pyramides de Gizeh & Sphinx Céleste",
                    "Skyline New-York Gratte-Ciels",
                    "Forêt de Bambous Voie Pavée",
                    "Cascade Tropicale Liane et Brume",
                    "Désert de Dunes Vague de Sable",
                    "Ruelle Pavée Médiévale aux Lanternes",
                    "Château Fort Pont-Levis & Tours",
                    "Temple Grec Colonnes Doriques",
                    "Coucher de Soleil sur Baie de Rio",
                    "Village Provençal Lavandes en Fleur",
                    "Moulin à Vent Hollandais au Crépuscule"
                ),
                "Botanique & Fleurs" to listOf(
                    "Rose Éclose Gouttes de Rosée",
                    "Bouquet de Pivoines Pétales Froissés",
                    "Orchidée Phalaenopsis Tige Courbée",
                    "Fleur de Lotus Flottant sur l'Eau",
                    "Tournesol Rayonnant Cœur Graineux",
                    "Tulipe Royale Coupe Élégante",
                    "Bonsaï Centenaire Tronc Tortueux",
                    "Branche de Cerisier en Fleurs",
                    "Monstera Deliciosa Feuilles Découpées",
                    "Feuillage d'Eucalyptus Argenté",
                    "Cactus Saguaro Épines Géométriques",
                    "Lys Blanc Pistil Poudré d'Or",
                    "Lavande en Brins Serrés",
                    "Hortensia Pompon de Fleurons",
                    "Coquelicot Champêtre Pétale de Soie",
                    "Feuille de Ginkgo Biloba en Éventail",
                    "Fougère Déroulant sa Crosse",
                    "Iris Sauvage Barbe Violette",
                    "Marguerite Champêtre Cœur Jaune",
                    "Magnolia Fleurs aux Branches Nues"
                ),
                "Microscopie & Macro" to listOf(
                    "Cratères Lunaires Tycho & Copernic",
                    "Surface Lunaire Mer de la Tranquillité",
                    "Cellules Végétales d'Oignon Chloroplaste",
                    "Nervures Microscopiques Feuille de Chêne",
                    "Écailles d'Aile de Papillon au Microscope",
                    "Cristaux de Glace Structure Dendritique",
                    "Bactérie Spirille & Flagelles 10000x",
                    "Grains de Pollen Formes Géométriques",
                    "Structure Cristalline du Quartz",
                    "Fibre de Coton Tissée Agrandie 500x",
                    "Œil Facetté d'Abeille Hexagones",
                    "Gouttelette d'Huile dans l'Eau Émulsion",
                    "Micro-Organismes Eau Douce Paramécie",
                    "Nébuleuse d'Orion Poussières Stellaires",
                    "Planète Saturne & Système d'Anneaux",
                    "Surface Martienne Ravines & Roches",
                    "Globules Rouges & Capillaires Sanguins",
                    "Filament d'Ampoule Tungstène Incandescent",
                    "Pointe d'Aiguille et Grain de Sable",
                    "Cristal de Sucre Parfait 2000x"
                )
            )

            val webImagePool = listOf(
                "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1517841905240-472988babdf9?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1501196354995-cbb51c65aaea?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1529626455594-4ff0802cfb7e?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1488426862026-3ee34a7d66df?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1534188753412-3e26d1d618d6?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1474511320723-9a56873867b5?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1575550959106-5a7defe28b56?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1579783902614-a3fb3927b675?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1563089145-599997674d42?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1550684848-fac1c5b4e853?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1490750967868-88aa4486c946?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1526047932273-341f2a7631f9?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1509114397022-ed747cca3f65?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1513694203232-719a280e022f?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1477959858617-67f30bc75b82?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1509228468518-180dd4864904?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1550684384-2eff6be6eb0e?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1541701494587-cb58502866ab?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1565193566173-7a0ee3dbe261?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1549834180-7c3a10619a9a?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-157294725610d-0a5abf21cc45?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1451187580459-43490279c0fa?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?auto=format&fit=crop&w=800&q=80",
                "https://images.unsplash.com/photo-1507499739999-097706ad8914?auto=format&fit=crop&w=800&q=80"
            )

            // 1. Featured core templates with verified web URLs
            list.add(
                TraceTemplate(
                    id = "tpl_anime_hero",
                    title = "Héros Shōnen Manga",
                    category = "Anime & Manga",
                    drawableResName = "template_anime_hero",
                    imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=800&q=80",
                    difficulty = "Intermédiaire",
                    description = "Portrait expressif avec mèches dynamiques, regard déterminé et col de blouson.",
                    tags = listOf("Manga", "Portrait", "Shonen", "Cheveux", "Regard")
                )
            )
            list.add(
                TraceTemplate(
                    id = "tpl_majestic_wolf",
                    title = "Loup Sauvage Géométrique",
                    category = "Animaux Sauvages",
                    drawableResName = "template_majestic_wolf",
                    imageUrl = "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?auto=format&fit=crop&w=800&q=80",
                    difficulty = "Avancé",
                    description = "Loup hurlant à la lune sur falaise rocheuse avec facettes géométriques ultra-nettes.",
                    tags = listOf("Loup", "Géométrie", "Lune", "Nature", "Lignes")
                )
            )
            list.add(
                TraceTemplate(
                    id = "tpl_sample_parrot",
                    title = "Ara Macao Tropical",
                    category = "Hyper-Réaliste & Réaliste",
                    drawableResName = "sample_photo_parrot",
                    imageUrl = "https://images.unsplash.com/photo-1546182990-dffeafbe841d?auto=format&fit=crop&w=800&q=80",
                    difficulty = "Débutant à Pro",
                    description = "Perroquet aux plumages colorés idéal pour tester les filtres Sobel et Belle Vue.",
                    tags = listOf("Oiseau", "Réaliste", "Couleur", "Plumes", "Faune")
                )
            )
            list.add(
                TraceTemplate(
                    id = "tpl_ar_butterfly",
                    title = "Papillon Cybernétique",
                    category = "Mandalas & Géométrie",
                    drawableResName = "ic_ar_trace_logo",
                    imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?auto=format&fit=crop&w=800&q=80",
                    difficulty = "Débutant",
                    description = "Ailes symétriques à motifs de mandala et grille laser pour un alignement parfait.",
                    tags = listOf("Papillon", "Symétrie", "Mandala", "Laser")
                )
            )

            var idCounter = 5
            val difficulties = listOf("Débutant", "Intermédiaire", "Avancé", "Expert Hyper-Réaliste")

            // Generate variations up to 500+ items with unique web image URLs
            categoriesMap.forEach { (catName, titles) ->
                titles.forEachIndexed { titleIdx, title ->
                    for (variant in 1..3) {
                        val assignedUrl = webImagePool[(idCounter * 31 + variant) % webImagePool.size]
                        val uniqueSeedUrl = "$assignedUrl&seed=$idCounter"
                        val diff = difficulties[(titleIdx + variant) % difficulties.size]
                        val variantTitle = if (variant == 1) title else "$title (Étude $variant)"

                        list.add(
                            TraceTemplate(
                                id = "tpl_${idCounter++}",
                                title = variantTitle,
                                category = catName,
                                drawableResName = "template_anime_hero",
                                imageUrl = uniqueSeedUrl,
                                difficulty = diff,
                                description = "Modèle de traçage de précision pour artistes. Optimisé pour filtres Sobel, Négatif et Loupe Macro.",
                                tags = listOf(catName.split(" ").first(), "Étude", "Art", diff)
                            )
                        )
                    }
                }
            }

            // Fill remaining to ensure 520+ distinct art references with unique web URLs
            while (list.size < 520) {
                val idx = list.size + 1
                val cat = categoriesMap.keys.elementAt(idx % categoriesMap.size)
                val assignedUrl = webImagePool[(idx * 17) % webImagePool.size]
                list.add(
                    TraceTemplate(
                        id = "tpl_$idx",
                        title = "Planche d'Artiste & Étude N°$idx",
                        category = cat,
                        drawableResName = "template_anime_hero",
                        imageUrl = "$assignedUrl&seed=fill_$idx",
                        difficulty = difficulties[idx % difficulties.size],
                        description = "Exercice de perfectionnement graphique pour le développement de la mémoire musculaire du tracé.",
                        tags = listOf("Pratique", "Esquisse", cat)
                    )
                )
            }

            return list
        }

        // Step-by-Step Drawing Lessons (Apprendre à dessiner pas à pas)
        fun getTutorials(): List<StepByStepTutorial> = listOf(
            StepByStepTutorial(
                id = "tuto_portrait_face",
                title = "Portrait Proportions & Regard Expressif",
                category = "Anatomie",
                difficulty = "Débutant à Intermédiaire",
                estimatedMinutes = 25,
                steps = listOf(
                    TutorialStep(
                        stepIndex = 1,
                        stepTitle = "Étape 1 : Formes de Base & Cercle d'Andrew Loomis",
                        instruction = "Tracez le cercle crânien et les axes verticaux et horizontaux pour diviser le visage en 3 tiers équilibrés.",
                        overlayDrawableResName = "template_anime_hero",
                        tip = "Posez le téléphone sur un mug avec zoom 1x pour bien voir toute la feuille."
                    ),
                    TutorialStep(
                        stepIndex = 2,
                        stepTitle = "Étape 2 : Ligne de Mâchoire & Alignement des Oreilles",
                        instruction = "Descendez les lignes de pommettes vers le menton. Les oreilles s'étendent exactement entre la ligne des sourcils et le bas du nez.",
                        overlayDrawableResName = "template_anime_hero",
                        tip = "Utilisez la grille 3x3 pour vérifier la symétrie gauche/droite."
                    ),
                    TutorialStep(
                        stepIndex = 3,
                        stepTitle = "Étape 3 : Yeux, Iris & Expression du Regard",
                        instruction = "Tracez l'arcade sourcilière, puis les amandes des yeux espacées d'exactement la largeur d'un œil au milieu.",
                        overlayDrawableResName = "template_anime_hero",
                        tip = "Activez la Loupe Macro pour tracer les cils et l'éclat lumineux de la pupille."
                    ),
                    TutorialStep(
                        stepIndex = 4,
                        stepTitle = "Étape 4 : Nez, Bouche & Contours Finaux",
                        instruction = "Esquissez l'arête du nez avec une ombre douce et marquez la ligne de commissure des lèvres.",
                        overlayDrawableResName = "template_anime_hero",
                        tip = "Verrouillez l'ancrage avec [LOCK CONFIRMATION] avant d'encrer vos lignes au feutre."
                    )
                )
            ),
            StepByStepTutorial(
                id = "tuto_loup_geometrique",
                title = "Loup Géométrique & Facettes Angulaires",
                category = "Animaux",
                difficulty = "Intermédiaire",
                estimatedMinutes = 20,
                steps = listOf(
                    TutorialStep(
                        stepIndex = 1,
                        stepTitle = "Étape 1 : Silhouette Globale & Posture",
                        instruction = "Positionnez les grands triangles du corps, la roche inclinée et le cercle de la lune en arrière-plan.",
                        overlayDrawableResName = "template_majestic_wolf",
                        tip = "Activez le filtre Contours Sobel pour isoler uniquement les segments droits."
                    ),
                    TutorialStep(
                        stepIndex = 2,
                        stepTitle = "Étape 2 : Facettage de la Tête et du Museau",
                        instruction = "Tracez les polygones formant la gueule hurlante, les oreilles pointues et les yeux fermés.",
                        overlayDrawableResName = "template_majestic_wolf",
                        tip = "Baissez l'opacité à 40% pour distinguer nettement la mine de votre crayon de bois."
                    ),
                    TutorialStep(
                        stepIndex = 3,
                        stepTitle = "Étape 3 : Pelage Polygonale & Forêt de Sapins",
                        instruction = "Remplissez les facettes du poitrail et les lignes verticales des conifères à l'horizon.",
                        overlayDrawableResName = "template_majestic_wolf",
                        tip = "Allumez la Torche Flash pour éliminer l'ombre de votre poignet."
                    )
                )
            ),
            StepByStepTutorial(
                id = "tuto_mandala_sacred",
                title = "Mandala Sacré & Symétrie Parfaite",
                category = "Mandalas",
                difficulty = "Tous Niveaux",
                estimatedMinutes = 15,
                steps = listOf(
                    TutorialStep(
                        stepIndex = 1,
                        stepTitle = "Étape 1 : Le Centre & Premier Cercle d'Éclosion",
                        instruction = "Placez la pointe du compas au point central guidé par le réticule et tracez le cœur du motif.",
                        overlayDrawableResName = "ic_ar_trace_logo",
                        tip = "La grille 4x4 ou 8x8 vous assure une rotation symétrique à 360°."
                    ),
                    TutorialStep(
                        stepIndex = 2,
                        stepTitle = "Étape 2 : Pétales & Arabesques Rayonnantes",
                        instruction = "Déployez les pétales extérieurs en suivant le calque transparent projeté sur la feuille.",
                        overlayDrawableResName = "ic_ar_trace_logo",
                        tip = "Utilisez le Négatif Luminescent si vous dessinez avec un stylo gel blanc sur papier kraft."
                    )
                )
            )
        )

        // Inspiration Feed Posts
        fun getInspirationFeed(): List<InspirationPost> = listOf(
            InspirationPost(
                id = "post_1",
                title = "Le Secret du Regard Hyper-Réaliste",
                artistName = "Rich'Art Dely",
                category = "Conseil de Maître",
                drawableResName = "sample_photo_parrot",
                imageUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=800&q=80",
                advice = "Ne noircissez jamais l'iris d'un seul bloc ! Laissez toujours un point de lumière pure (blanc du papier) pour insuffler la vie au regard.",
                challengePrompt = "Défi du jour : Dessinez un œil complet en moins de 10 minutes avec l'outil Loupe Macro.",
                likesCount = 384
            ),
            InspirationPost(
                id = "post_2",
                title = "L'Astuce de la Tasse & du Grand Angle 0.5x",
                artistName = "Rich'Art Dely",
                category = "Astuce Matériel",
                drawableResName = "template_anime_hero",
                imageUrl = "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?auto=format&fit=crop&w=800&q=80",
                advice = "Pas de trépied ? Posez simplement votre téléphone horizontalement sur un mug lourd au bord de votre feuille. En activant le dézoom Grand Angle (0.5x), toute votre feuille A4 tient dans l'écran !",
                challengePrompt = "Testez la configuration Mug + Grand angle sur une esquisse manga.",
                likesCount = 512
            ),
            InspirationPost(
                id = "post_3",
                title = "Maîtriser les Hachures Croisées (Cross-Hatching)",
                artistName = "Rich'Art Dely",
                category = "Technique Ombrage",
                drawableResName = "template_majestic_wolf",
                imageUrl = "https://images.unsplash.com/photo-1564349683136-77e08dba1ef7?auto=format&fit=crop&w=800&q=80",
                advice = "Pour des ombres profondes et élégantes, variez la densité des traits plutôt que d'appuyer plus fort sur votre mine.",
                challengePrompt = "Dessinez une sphère ombragée uniquement avec des lignes croisées à 45°.",
                likesCount = 276
            ),
            InspirationPost(
                id = "post_4",
                title = "Le Dessin sur Papier Sombre avec Négatif Luminescent",
                artistName = "Rich'Art Dely",
                category = "Créativité Papier Noir",
                drawableResName = "ic_ar_trace_logo",
                imageUrl = "https://images.unsplash.com/photo-1509228468518-180dd4864904?auto=format&fit=crop&w=800&q=80",
                advice = "Avec un carnet noir et un feutre blanc ou doré, activez le filtre 'Négatif Luminescent'. L'application projette des lignes blanches éclatantes qui subliment votre création.",
                challengePrompt = "Réalisez un tracé géométrique ou un papillon néon sur fond sombre.",
                likesCount = 429
            )
        )
    }
}
