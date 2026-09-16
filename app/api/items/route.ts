import { NextRequest, NextResponse } from 'next/server'
import { isAuthorized } from '@/lib/auth'
import { db } from '@/lib/db'
import { items } from '@/lib/db/schema'
import { desc } from 'drizzle-orm'

export async function GET(req: NextRequest) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const all = await db.select().from(items).orderBy(desc(items.useCount))
  return NextResponse.json(all)
}

export async function POST(req: NextRequest) {
  if (!isAuthorized(req)) return NextResponse.json({ error: 'Unauthorized' }, { status: 401 })

  const { name } = await req.json()
  if (!name || typeof name !== 'string') {
    return NextResponse.json({ error: 'name is required' }, { status: 400 })
  }

  // the drinks shortlist is closed — anything added through this endpoint is food
  try {
    const [created] = await db.insert(items).values({ name, category: 'food' }).returning()
    return NextResponse.json(created)
  } catch (e) {
    if (e instanceof Error && e.message.includes('duplicate key')) {
      return NextResponse.json({ error: 'An item with this name already exists' }, { status: 409 })
    }
    throw e
  }
}
