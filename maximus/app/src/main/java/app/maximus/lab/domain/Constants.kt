package app.maximus.lab.domain

/**
 * Physical constants, CODATA 2018 recommended values (NIST, May 2019). Since the 2019 SI revision
 * c, h, e, k_B and N_A are exact by definition; derived quantities such as ħ, R and σ are therefore
 * exact as well (shown truncated). [relUncertainty] is the relative standard uncertainty.
 */
data class PhysConst(
    val key: String,
    val symbol: String,
    val name: String,
    val value: Double,
    val unit: String,
    val relUncertainty: Double = 0.0,
    val group: String
) {
    val exact: Boolean get() = relUncertainty == 0.0
}

object Phys {
    const val c = 299_792_458.0
    const val h = 6.62607015e-34
    const val hbar = 1.054571817e-34
    const val e = 1.602176634e-19
    const val kB = 1.380649e-23
    const val NA = 6.02214076e23
    const val R = 8.314462618
    const val sigmaSB = 5.670374419e-8
    const val eps0 = 8.8541878128e-12
    const val mu0 = 1.25663706212e-6
    const val me = 9.1093837015e-31
    const val mp = 1.67262192369e-27
    const val mn = 1.67492749804e-27
    const val u = 1.66053906660e-27
    const val G = 6.67430e-11
    const val g0 = 9.80665
    const val alpha = 7.2973525693e-3
    const val a0 = 5.29177210903e-11
    const val Rinf = 10_973_731.568160
    const val RyEv = 13.605693122994
    const val muB = 9.2740100783e-24
    const val muN = 5.0507837461e-27
    const val wienB = 2.897771955e-3
    const val phi0 = 2.067833848e-15
    const val G0 = 7.748091729e-5
    const val RK = 25_812.80745
    const val lambdaC = 2.42631023867e-12
    const val re = 2.8179403262e-15
    const val sigmaT = 6.6524587321e-29
    const val atm = 101_325.0

    /** Energies in MeV and the conversion ħc for natural units. */
    const val meMeV = 0.51099895000
    const val mpMeV = 938.27208816
    const val mnMeV = 939.56542052
    const val mmuMeV = 105.6583755
    const val mtauMeV = 1776.86
    const val hbarcMeVfm = 197.3269804
    const val hbarMeVs = 6.582119569e-22
    /** Fermi constant G_F/(ħc)³ in GeV⁻². */
    const val GF = 1.1663787e-5
    /** (ħc)² in GeV² mb, for converting cross-sections from natural units. */
    const val hbarc2GeV2mb = 0.3893793721

    val all: List<PhysConst> = listOf(
        PhysConst("c", "c", "Lichtgeschwindigkeit im Vakuum", c, "m s⁻¹", group = "Definierend (exakt)"),
        PhysConst("h", "h", "Planck-Konstante", h, "J s", group = "Definierend (exakt)"),
        PhysConst("e", "e", "Elementarladung", e, "C", group = "Definierend (exakt)"),
        PhysConst("kB", "k_B", "Boltzmann-Konstante", kB, "J K⁻¹", group = "Definierend (exakt)"),
        PhysConst("NA", "N_A", "Avogadro-Konstante", NA, "mol⁻¹", group = "Definierend (exakt)"),
        PhysConst("hbar", "ħ", "Reduzierte Planck-Konstante h/2π", hbar, "J s", group = "Abgeleitet (exakt)"),
        PhysConst("R", "R", "Molare Gaskonstante N_A k_B", R, "J mol⁻¹ K⁻¹", group = "Abgeleitet (exakt)"),
        PhysConst("sigma", "σ", "Stefan-Boltzmann-Konstante", sigmaSB, "W m⁻² K⁻⁴", group = "Abgeleitet (exakt)"),
        PhysConst("F", "F", "Faraday-Konstante N_A e", NA * e, "C mol⁻¹", group = "Abgeleitet (exakt)"),
        PhysConst("wien", "b", "Wiensche Verschiebungskonstante", wienB, "m K", group = "Abgeleitet (exakt)"),
        PhysConst("phi0", "Φ₀", "Magnetisches Flussquant h/2e", phi0, "Wb", group = "Abgeleitet (exakt)"),
        PhysConst("G0", "G₀", "Leitwertquant 2e²/h", G0, "S", group = "Abgeleitet (exakt)"),
        PhysConst("RK", "R_K", "von-Klitzing-Konstante h/e²", RK, "Ω", group = "Abgeleitet (exakt)"),
        PhysConst("eps0", "ε₀", "Elektrische Feldkonstante", eps0, "F m⁻¹", 1.5e-10, "Elektromagnetismus"),
        PhysConst("mu0", "μ₀", "Magnetische Feldkonstante", mu0, "N A⁻²", 1.5e-10, "Elektromagnetismus"),
        PhysConst("alpha", "α", "Feinstrukturkonstante e²/4πε₀ħc", alpha, "1", 1.5e-10, "Elektromagnetismus"),
        PhysConst("alphaInv", "α⁻¹", "Inverse Feinstrukturkonstante", 137.035999084, "1", 1.5e-10, "Elektromagnetismus"),
        PhysConst("muB", "μ_B", "Bohrsches Magneton eħ/2mₑ", muB, "J T⁻¹", 3.0e-10, "Elektromagnetismus"),
        PhysConst("muN", "μ_N", "Kernmagneton", muN, "J T⁻¹", 3.1e-10, "Elektromagnetismus"),
        PhysConst("me", "mₑ", "Elektronenmasse", me, "kg", 3.0e-10, "Teilchen"),
        PhysConst("mp", "mₚ", "Protonenmasse", mp, "kg", 3.1e-10, "Teilchen"),
        PhysConst("mn", "mₙ", "Neutronenmasse", mn, "kg", 5.7e-10, "Teilchen"),
        PhysConst("u", "u", "Atomare Masseneinheit", u, "kg", 3.0e-10, "Teilchen"),
        PhysConst("meMeV", "mₑc²", "Ruheenergie Elektron", meMeV, "MeV", 3.0e-10, "Teilchen"),
        PhysConst("mpMeV", "mₚc²", "Ruheenergie Proton", mpMeV, "MeV", 3.1e-10, "Teilchen"),
        PhysConst("mmu", "m_μc²", "Ruheenergie Myon", mmuMeV, "MeV", 2.2e-8, "Teilchen"),
        PhysConst("a0", "a₀", "Bohrscher Radius", a0, "m", 1.5e-10, "Atomphysik"),
        PhysConst("Rinf", "R_∞", "Rydberg-Konstante", Rinf, "m⁻¹", 1.9e-12, "Atomphysik"),
        PhysConst("Ry", "Ry", "Rydberg-Energie hcR_∞", RyEv, "eV", 1.9e-12, "Atomphysik"),
        PhysConst("lambdaC", "λ_C", "Compton-Wellenlänge des Elektrons h/mₑc", lambdaC, "m", 3.0e-10, "Atomphysik"),
        PhysConst("re", "rₑ", "Klassischer Elektronenradius", re, "m", 4.5e-10, "Atomphysik"),
        PhysConst("sigmaT", "σ_T", "Thomson-Wirkungsquerschnitt", sigmaT, "m²", 9.1e-10, "Atomphysik"),
        PhysConst("hbarc", "ħc", "Umrechnung natürliche Einheiten", hbarcMeVfm, "MeV fm", group = "Teilchenphysik"),
        PhysConst("hbarMeV", "ħ", "Reduzierte Planck-Konstante", hbarMeVs, "MeV s", group = "Teilchenphysik"),
        PhysConst("GF", "G_F/(ħc)³", "Fermi-Konstante", GF, "GeV⁻²", 5.1e-7, "Teilchenphysik"),
        PhysConst("hbarc2", "(ħc)²", "Umrechnung Wirkungsquerschnitt", hbarc2GeV2mb, "GeV² mb", 2.2e-10, "Teilchenphysik"),
        PhysConst("G", "G", "Gravitationskonstante", G, "m³ kg⁻¹ s⁻²", 2.2e-5, "Gravitation"),
        PhysConst("g0", "g₀", "Normfallbeschleunigung (Konvention)", g0, "m s⁻²", group = "Gravitation"),
        PhysConst("atm", "atm", "Standardatmosphäre (Konvention)", atm, "Pa", group = "Thermodynamik"),
        PhysConst("Vm", "V_m", "Molvolumen ideales Gas (273,15 K, 101,325 kPa)", R * 273.15 / atm, "m³ mol⁻¹", group = "Thermodynamik")
    )
}
