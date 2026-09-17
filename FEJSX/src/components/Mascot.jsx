// Mascotte SVG che saluta: braccio animato via CSS, occhi che sbattono,
// leggero "float" del corpo. Nessuna dipendenza, nessuna immagine.

export default function Mascot() {
  return (
    <svg
      className="mascot"
      viewBox="0 0 180 170"
      role="img"
      aria-label="Un piccolo robot che saluta"
    >
      {/* ombra a terra */}
      <ellipse className="mascot-shadow" cx="88" cy="156" rx="40" ry="7" />

      <g className="mascot-body">
        {/* antenna */}
        <line x1="88" y1="34" x2="88" y2="20" className="mascot-stroke" />
        <circle cx="88" cy="16" r="6" className="mascot-antenna" />

        {/* testa */}
        <rect
          x="44"
          y="34"
          width="88"
          height="72"
          rx="24"
          className="mascot-head"
        />

        {/* visiera */}
        <rect
          x="56"
          y="48"
          width="64"
          height="44"
          rx="18"
          className="mascot-visor"
        />

        {/* occhi */}
        <g className="mascot-eyes">
          <circle cx="76" cy="70" r="6" className="mascot-eye" />
          <circle cx="100" cy="70" r="6" className="mascot-eye" />
        </g>

        {/* sorriso */}
        <path
          d="M78 84q10 8 20 0"
          className="mascot-smile"
          fill="none"
        />

        {/* orecchie */}
        <rect x="36" y="58" width="9" height="24" rx="4.5" className="mascot-ear" />
        <rect x="131" y="58" width="9" height="24" rx="4.5" className="mascot-ear" />

        {/* corpo */}
        <rect
          x="58"
          y="110"
          width="60"
          height="40"
          rx="16"
          className="mascot-torso"
        />

        {/* braccio fermo */}
        <path
          d="M58 122 L40 134"
          className="mascot-stroke mascot-arm-static"
        />
        <circle cx="37" cy="136" r="6" className="mascot-hand" />
      </g>

      {/* braccio che saluta */}
      <g className="mascot-wave">
        <path d="M118 122 L140 110" className="mascot-stroke" />
        <circle cx="143" cy="108" r="7" className="mascot-hand" />
      </g>
    </svg>
  )
}
