package com.example.githubsearchrepo.events

import com.example.githubsearchrepo.database.GithubItemEntity
import io.realm.RealmResults

class FilterUpdateEvent(val githubItemEntities: RealmResults<GithubItemEntity>)