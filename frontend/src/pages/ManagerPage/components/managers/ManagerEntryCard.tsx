import { useState } from 'react'

import type { ManagerEntryCardProps } from '@/pages/ManagerPage/objects'

export function ManagerEntryCard({ title, shortTitle, accentClassName, imageSrc, imageAlt, onSelect }: ManagerEntryCardProps) {
  const [isFlipped, setIsFlipped] = useState(false)

  return (
    <article className="grid justify-items-center gap-6">
      <div className="grid justify-items-center gap-8">
        <div
          className="h-40 w-40 [perspective:1200px]"
          onMouseEnter={() => setIsFlipped(true)}
          onMouseLeave={() => setIsFlipped(false)}
          onFocus={() => setIsFlipped(true)}
          onBlur={() => setIsFlipped(false)}
        >
          <div
            className="relative h-full w-full transition-transform duration-500 [transform-style:preserve-3d]"
            style={{ transform: isFlipped ? 'rotate(45deg) rotateY(180deg)' : 'rotate(45deg)' }}
          >
            <div className={`absolute inset-0 grid place-items-center shadow-xl shadow-slate-300/60 [backface-visibility:hidden] ${accentClassName}`}>
              <div className="-rotate-45 grid justify-items-center gap-2 text-center text-white">
                <span className="text-2xl font-bold tracking-wide">{shortTitle}</span>
                <span className="max-w-28 text-base font-bold leading-tight">{title}</span>
              </div>
            </div>

            <div className={`absolute inset-0 grid place-items-center overflow-hidden text-white shadow-xl shadow-slate-300/80 [backface-visibility:hidden] [transform:rotateY(180deg)] ${accentClassName}`}>
              <div className="-rotate-45 grid h-full w-full grid-cols-[1fr_auto_1fr] items-center px-3">
                <button
                  type="button"
                  className="grid h-full w-full place-items-center border-0 bg-transparent p-0 font-['SimSun','瀹嬩綋',serif] text-lg font-bold text-white shadow-none transition hover:text-slate-950 focus:text-slate-950"
                  onClick={() => onSelect('register')}
                >
                  注册
                </button>
                <img src="/images/manager-entry/fly-bara-entry.png" alt="" aria-hidden="true" className="h-11 w-11 object-contain" />
                <button
                  type="button"
                  className="grid h-full w-full place-items-center border-0 bg-transparent p-0 font-['SimSun','瀹嬩綋',serif] text-lg font-bold text-white shadow-none transition hover:text-slate-950 focus:text-slate-950"
                  onClick={() => onSelect('login')}
                >
                  登录
                </button>
              </div>
            </div>
          </div>
        </div>
        <h3 className="m-0 text-center text-2xl font-bold leading-tight text-slate-700">{title}</h3>
      </div>

      <img src={imageSrc} alt={imageAlt} className="h-52 w-full border border-slate-200 object-cover shadow-sm shadow-slate-200/70" />
    </article>
  )
}
