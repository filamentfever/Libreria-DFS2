// Íconos SVG propios (sin dependencias) usados por la tienda estilo librería.
import type { SVGProps } from "react"

type Props = SVGProps<SVGSVGElement>

const base = {
  fill: "none",
  stroke: "currentColor",
  strokeWidth: 1.8,
  strokeLinecap: "round" as const,
  strokeLinejoin: "round" as const,
  viewBox: "0 0 24 24",
  "aria-hidden": true,
}

export const SearchIcon = (props: Props) => (
  <svg {...base} {...props}>
    <circle cx="11" cy="11" r="7" />
    <path d="m20 20-3.5-3.5" />
  </svg>
)

export const BagIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M6 8h12l1 12H5L6 8Z" />
    <path d="M9 8V6a3 3 0 0 1 6 0v2" />
  </svg>
)

export const UserIcon = (props: Props) => (
  <svg {...base} {...props}>
    <circle cx="12" cy="8" r="3.5" />
    <path d="M5 20c.7-3.6 3.4-5.5 7-5.5s6.3 1.9 7 5.5" />
  </svg>
)

export const HeartIcon = ({ relleno = false, ...props }: Props & { relleno?: boolean }) => (
  <svg {...base} fill={relleno ? "currentColor" : "none"} {...props}>
    <path d="M12 20s-7-4.4-7-9.3A4 4 0 0 1 12 7a4 4 0 0 1 7 3.7c0 4.9-7 9.3-7 9.3Z" />
  </svg>
)

export const MenuIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M4 7h16M4 12h16M4 17h16" />
  </svg>
)

export const CloseIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M6 6l12 12M18 6 6 18" />
  </svg>
)

export const ChevronDownIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="m6 9 6 6 6-6" />
  </svg>
)

export const ChevronLeftIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="m14 6-6 6 6 6" />
  </svg>
)

export const ChevronRightIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="m10 6 6 6-6 6" />
  </svg>
)

export const ArrowRightIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M4 12h16M14 6l6 6-6 6" />
  </svg>
)

export const TruckIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M3 7h11v9H3zM14 10h4l3 3v3h-7z" />
    <circle cx="7" cy="18" r="1.6" />
    <circle cx="17.5" cy="18" r="1.6" />
  </svg>
)

export const StoreIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M4 10v10h16V10M3 10l2-5h14l2 5" />
    <path d="M3 10a2.5 2.5 0 0 0 5 0 2.5 2.5 0 0 0 4 0 2.5 2.5 0 0 0 4 0 2.5 2.5 0 0 0 5 0" />
    <path d="M10 20v-5h4v5" />
  </svg>
)

export const ShieldIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M12 3 5 6v5c0 4.4 3 7.6 7 9 4-1.4 7-4.6 7-9V6l-7-3Z" />
    <path d="m9 12 2 2 4-4" />
  </svg>
)

export const CardIcon = (props: Props) => (
  <svg {...base} {...props}>
    <rect x="3" y="6" width="18" height="12" rx="2" />
    <path d="M3 10h18M6.5 15h3" />
  </svg>
)

export const BookIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M12 6c-1.6-1.4-3.7-2-6-2v14c2.3 0 4.4.6 6 2 1.6-1.4 3.7-2 6-2V4c-2.3 0-4.4.6-6 2Z" />
    <path d="M12 6v14" />
  </svg>
)

export const MailIcon = (props: Props) => (
  <svg {...base} {...props}>
    <rect x="3" y="5" width="18" height="14" rx="2" />
    <path d="m4 7 8 6 8-6" />
  </svg>
)

export const PinIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M12 21s7-5.8 7-11a7 7 0 1 0-14 0c0 5.2 7 11 7 11Z" />
    <circle cx="12" cy="10" r="2.6" />
  </svg>
)

export const EyeIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M2.5 12S6 5.5 12 5.5 21.5 12 21.5 12 18 18.5 12 18.5 2.5 12 2.5 12Z" />
    <circle cx="12" cy="12" r="3" />
  </svg>
)

export const EyeOffIcon = (props: Props) => (
  <svg {...base} {...props}>
    <path d="M4 4l16 16" />
    <path d="M9.9 5.9A9.8 9.8 0 0 1 12 5.5c6 0 9.5 6.5 9.5 6.5a17 17 0 0 1-3 3.9" />
    <path d="M6.3 7.2A17 17 0 0 0 2.5 12S6 18.5 12 18.5a9.4 9.4 0 0 0 3.6-.7" />
    <path d="M9.9 9.9a3 3 0 0 0 4.2 4.2" />
  </svg>
)

